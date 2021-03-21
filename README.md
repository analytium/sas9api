# sas9api


#ssl key store
To list contents of the keystore locate to keystore file and type:
    keytool -list -v -storetype pkcs12 -keystore localhost-ssl-keystore.p12
    enter password sas9api (more info in application.yml)
   
Extract an SSL certificate from a keystore:
   keytool -export -keystore localhost-ssl-keystore.p12 -alias selfsigned_localhost_sslserver -file {insert filename}

Create a keystore:
    keytool -genkey -alias {{somealias}} -keystore {{somekeystore.p12}} -storetype PKCS12 -keyalg RSA -storepass {{somepass}} -validity 730 -keysize 4096

Create a PKCS12 keystore from an existing private key and certificate using openssl:
   openssl pkcs12 -export -in certificate.pem -inkey key.pem -out localhost-ssl-keystore.p12
   
Extract a private key from a keystore using openssl:
   openssl pkcs12 -in localhost-ssl-keystore.p12 -nocerts -nodes
   
Extract certificates from a keystore using openssl
   openssl pkcs12 -in localhost-ssl-keystore.p12 -nokeys
   
