package vbakaev.app.config

final case class ServerConfig(
    appRoot: String,
    interface: String,
    port: Int
)

final case class MailjetConfig(
    apiKey: String,
    apiSecret: String
)

final case class MongoDBConfig(
    uri: String,
    database: String
)

final case class MailConfig(
    host: String,
    sender: String
)

final case class JwtConfig(
    secretKey: String,
    algo: String
)

final case class AppConfig(
    http: ServerConfig,
    mailjet: MailjetConfig,
    mongo: MongoDBConfig,
    mail: MailConfig,
    jwt: JwtConfig
)
