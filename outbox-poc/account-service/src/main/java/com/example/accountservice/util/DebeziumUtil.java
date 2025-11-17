package com.example.accountservice.util;

import com.example.accountservice.service.OutboxService;
import io.debezium.config.Configuration;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.embedded.Connect;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.debezium.data.Envelope.FieldName.AFTER;
import static io.debezium.data.Envelope.FieldName.OPERATION;

@Slf4j
@Component
public class DebeziumUtil {
    // Debezium CDC motorunu başlatır ve veritabanı değişikliklerini yakalar

    // Tek thread'li executor - değişikliklerin sıralı işlenmesi için
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Debezium motoru - veritabanı değişikliklerini dinler
    // ChangeEvent kullanıyoruz (RecordChangeEvent değil!)
    private final DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> engine;

    // Yakalanan değişiklikleri işleyen servis
    private final OutboxService outboxService;

    public DebeziumUtil(Properties debeziumProperties, OutboxService outboxService) {
        this.outboxService = outboxService;

        // Properties'i Debezium Configuration'a çevir
        Configuration config = Configuration.from(debeziumProperties);

        // Debezium Engine'i oluştur - Connect.class kullanarak
        // Connect.class, SourceRecord formatını destekleyen bir SerializationFormat
        this.engine = DebeziumEngine.create(Connect.class)
                // Veritabanı bağlantı ve tablo ayarları
                .using(config.asProperties())
                // Her değişiklikte handleEvent metodunu çağır
                .notifying(this::handleEvent)
                // Engine'i oluştur
                .build();
    }

    // Uygulama başladığında otomatik çalışır
    @PostConstruct
    public void start() {
        // Engine'i arka planda başlat (blocking olduğu için ayrı thread)
        executor.execute(engine);
        log.info("Debezium engine başlatıldı - Veritabanı değişiklikleri dinleniyor...");
    }

    // Uygulama kapanırken kaynaları temizle
    @PreDestroy
    public void stop() throws IOException {
        if (engine != null) {
            // Debezium engine'i kapat - veritabanı bağlantılarını temizle
            engine.close();
            log.info("Debezium engine durduruldu");
        }
        // Executor'ü kapat - thread'leri sonlandır
        executor.shutdown();
    }

    // Her veritabanı değişikliği için çağrılır
    // ChangeEvent parametresi kullanıyoruz (RecordChangeEvent değil!)
    private void handleEvent(ChangeEvent<SourceRecord, SourceRecord> event) {
        // Değişiklik kaydını al - key() ve value() metodları var
        SourceRecord sourceRecord = event.value();

        // Eğer kayıt null ise (silme işlemi olabilir)
        if (sourceRecord == null) {
            return;
        }

        // Değişikliğin içeriğini Struct formatında al
        // Struct: Kafka Connect'in kullandığı bir veri yapısı - JSON benzeri
        Struct sourceRecordValue = (Struct) sourceRecord.value();

        // Boş kayıtları atla
        if (sourceRecordValue == null) {
            return;
        }

        // Hangi operasyon yapıldı?
        // 'c' = CREATE (INSERT)
        // 'u' = UPDATE
        // 'd' = DELETE
        // 'r' = READ (snapshot - ilk başlatmada mevcut veriler)
        String operation = sourceRecordValue.getString(OPERATION);

        // Sadece INSERT (c) ve UPDATE (u) işlemlerini ele al
        // DELETE işlemlerini göz ardı ediyoruz çünkü outbox pattern'de
        // mesaj gönderildikten sonra kayıt zaten silinir
        if ("c".equals(operation) || "u".equals(operation)) {
            // AFTER: Değişiklikten SONRA satırın durumunu al
            // (BEFORE ise değişiklikten önceki durumu verir)
            Struct struct = (Struct) sourceRecordValue.get(AFTER);

            if (struct != null) {
                // Struct'ı Java Map'e dönüştür - daha kolay işlemek için
                Map<String, Object> payload = struct.schema().fields().stream()
                        // Tüm kolonları al (id, type, payload, created_at vs.)
                        .filter(field -> struct.get(field) != null) // Null olan kolonları atla
                        .collect(Collectors.toMap(
                                Field::name,               // Kolon adını key yap
                                field -> struct.get(field) // Kolon değerini value yap
                        ));

                // İşlenmiş veriyi OutboxService'e gönder
                // OutboxService şunları yapar:
                // 1. "type" field'ına göre doğru Kafka topic'ini belirler
                // 2. "payload" içindeki JSON verisini Kafka'ya mesaj olarak gönderir
                // 3. Mesaj başarıyla gönderildikten sonra bu kaydı outbox tablosundan siler
                // 4. Bu sayede hem transactional tutarlılık hem de at-least-once delivery garanti edilir
                outboxService.debeziumDatabaseChange(payload);

                log.debug("Değişiklik işlendi: operasyon={}, veri={}", operation, payload);
            }
        }
    }
}