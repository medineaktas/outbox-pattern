package com.example.accountservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class DebeziumConfig {
    // application.yml'den veritabanı bağlantı bilgilerini al

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    // Debezium için yapılandırma ayarlarını içeren Properties objesi oluştur
    @Bean
    public Properties debeziumProperties() {
        Properties props = new Properties();

        // ========== TEMEL AYARLAR ==========

        // Hangi Debezium connector'ı kullanacağız?
        // PostgresConnector: PostgreSQL veritabanlarını dinler
        props.setProperty("connector.class", "io.debezium.connector.postgresql.PostgresConnector");

        // Offset Storage: Debezium nereden okumaya devam edeceğini nasıl hatırlıyor?
        // MemoryOffsetBackingStore: RAM'de sakla (uygulama kapanınca unutulur)
        // Alternatif: FileOffsetBackingStore (dosyaya yaz, kalıcı olsun)
        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.MemoryOffsetBackingStore");

        // ========== CONNECTOR KİMLİĞİ ==========

        // Connector'a benzersiz bir isim ver
        props.setProperty("name", "account-postgres-connector");

        // Kafka topic'lerinin prefix'i (başlangıcı)
        // Örnek: "account.public.outboxs" şeklinde topic oluşturur
        props.setProperty("topic.prefix", "account");

        // ========== VERİTABANI BAĞLANTISI ==========

        // PostgreSQL sunucusunun adresi
        props.setProperty("database.hostname", "localhost");

        // PostgreSQL port'u
        props.setProperty("database.port", "5432");

        // Veritabanı kullanıcı adı (application.yml'den)
        props.setProperty("database.user", databaseUsername);

        // Veritabanı şifresi (application.yml'den)
        props.setProperty("database.password", databasePassword);

        // Hangi veritabanını dinleyeceğiz?
        props.setProperty("database.dbname", "outbox_poc");

        // Veritabanı sunucusuna mantıksal bir isim ver
        // Bu isim Kafka topic isimlerinde kullanılır
        props.setProperty("database.server.name", "account-db");

        // ========== TABLO FİLTRELEME ==========

        // Sadece belirli tabloları dinle
        // Format: "schema.tablo_adi"
        // public.outboxs: Sadece outboxs tablosundaki değişiklikleri yakala
        // Virgülle ayırarak birden fazla tablo ekleyebilirsiniz
        props.setProperty("table.include.list", "public.outboxs");

        // ========== POSTGRESQL ÖZELLEŞTİRME ==========

        // PostgreSQL'in WAL (Write-Ahead Log) okuma eklentisi
        // pgoutput: PostgreSQL'in yerleşik mantıksal replikasyon eklentisi
        // Alternatifler: wal2json, decoderbufs (ekstra kurulum gerekir)
        props.setProperty("plugin.name", "pgoutput");

        // ========== EK ÖNERİLEN AYARLAR ==========

        // Snapshot modu: İlk başlatmada mevcut verileri nasıl oku?
        // "initial": İlk başlatmada tüm mevcut kayıtları oku
        // "never": Sadece yeni değişiklikleri yakala
        // props.setProperty("snapshot.mode", "initial");

        // Schema değişikliklerini de kaydet mi?
        // props.setProperty("include.schema.changes", "false");

        return props;
        // Bu Properties objesi DebeziumUtil'e inject edilir
    }
}