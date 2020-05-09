package vbakaev.app.config

final case class ServerConfig(
    interface: String,
    port: Int
)

final case class MailjetConfig(
    apiKey: String,
    apiSecret: String
)

final case class MongoDBConfiguration(
    uri: String,
    database: String,
    collection: String
)

final case class MailConfig(
    host: String,
    sender: String
)

final case class AppConfig(
    http: ServerConfig,
    mailjet: MailjetConfig,
    mongo: MongoDBConfiguration,
    mail: MailConfig
)
