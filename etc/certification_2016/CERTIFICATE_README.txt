Code Signage Certificate Documentation 
April 22 2013
Expiration in 5 Years

1 - Generate Key Pair with RSA Algorithm and 2048 length
keytool -genkeypair -alias varylab -keysize 2048 -keyalg rsa -keystore varylab_request.keystore
password: varylab123
CN=GRP: VaryLab Developer Group TU-Berlin, 
OU=Institute of Mathematics - Geometry Group, 
O=Technische Universitaet Berlin, 
L=Berlin, 
ST=Berlin, 
C=DE
CN will be visible on the Webstart splash.

2 - From this keystore generate a certificate request to upload to the tubit
keytool -certreq -alias varylab -keystore varylab_request.keystore -file varylab_request.pem

3 - Go to the tubit website and upload the file varylabTUCertreq.pem
https://pki.pca.dfn.de/tu-berlin-ca/pub
the link Serverzertifikate will lead to the form
PIN: varylab123

4 - Hand in a hard copy of the request at the secretary of the tubit

5 - You will receive an email with the signed certificate 
cert-7631136256753072.pem

6 - With keytool create a PKCS12 keystore from the java keystore to export the private key with openssl
keytool -importkeystore -srckeystore varylab_request.keystore -destkeystore varylab_request.p12 -deststoretype PKCS12
password: varylab123

7 - export the private key to a pem file 
openssl pkcs12 -in varylab_request.p12 -nocerts -out varylab_privatekey.pem

8 - Downlaod the tubit CA key chain from 
https://pki.pca.dfn.de/tu-berlin-ca/pub/cacert/chain.txt

9 - Assemble the certificate chain, the private key, and the personal certificate
openssl pkcs12 -export -in cert-7631136256753072.pem -inkey varylab_privatekey.pem -certfile chain.txt  > varylab_signed.p12
export password: varylab123

10 - Convert the pkcs12 keystore to a java keystore the alias then will be "1".
keytool -importkeystore -srckeystore varylab_signed.p12 -destkeystore varylab_signed_2016.keystore -srcstoretype pkcs12

11 - Store the certificate in a secure location (Stefans Mac Book, sent to Thilo via encrypted Mail). 