Code Signage Certificate Documentation 
April 22 2013
Expiration in 5 Years

1 - Generate Key Pair with RSA Algorithm and 2048 length
keytool -genkeypair -alias varylab -keysize 2048 -keyalg rsa -keystore varylabTU.keystore
e.g. with this information
CN=Stefan Sechelmann, 
OU=Institut fuer Mathematik AG Geometrie, 
O=Technische Universitaet Berlin, 
L=Berlin, 
ST=Berlin, 
C=DE
CN will be visible on the Webstart splash. My choice here was not optimal since it shows
my name instead of varylab. Use varylab.com or similar the next time.

2 - From this keystore generate a certificate request to upload to the tubit
keytool -certreq -alias varylab -keystore varylabTU.keystore -file varylabTUCertreq.pem

3 - Go to the tubit website and upload the file varylabTUCertreq.pem
https://pki.pca.dfn.de/tu-berlin-ca/pub
the link Serverzertifikate will lead to the form

4 - Hand in a hard copy of the request at the secretary of the tubit

5 - You will receive an email with the signed certificate e.g. cert-1726317231523.pem

6 - With keytool create a PKCS12 keystore from the java keystore to export the private key with openssl
keytool -importkeystore -srckeystore varylabTU.keystore -destkeystore varylabTU.p12 -deststoretype PKCS12

7 - export the private key to a pem file 
openssl pkcs12 -in varylabTU.p12 -nocerts -out varylabTU.pem

8 - Downlaod the tubit CA key chain from 
https://pki.pca.dfn.de/tu-berlin-ca/pub/cacert/chain.txt

9 - Assemble the certificate chain, the private key, and the personal certificate
openssl pkcs12 -export -in cert-6074336525272796.pem -inkey varylabTU.pem -certfile chain.txt  > varylabTUSigned.p12

10 - Convert the pkcs12 keystore to a java keystore the alias then will be "1".
keytool -importkeystore -srckeystore varylabTUSigned.p12 -destkeystore varylabTUSigned.keystore -srcstoretype pkcs12