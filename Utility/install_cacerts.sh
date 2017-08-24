#!/bin/bash
# Create By: naorngsak.mala@ega.or.th
# Use for downlading and storing Certificate
 
if [ "$JAVA_HOME" == "" ]; then 
echo "\$JAVA_HOME hasn't been set\n"
else
echo "\$JAVA_HOME=$JAVA_HOME"
fi

default_host="203.150.62.10"
default_port="443"

read -p "Please enter IP[$default_host]: " host
read -p "Please enter Port[$default_port]: " port

if [ "$host" == "" ]; then
host=$default_host
fi

if [ "$port" == "" ]; then
port=$default_port
fi


echo "host:port > $host:$port"
echo -n | openssl s_client -connect $host:$port | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > ~/$host.crt

default_keypass=P@ssw0rd
default_storepass=P@ssw0rd

read -p "Please enter Keypass[$default_keypass]: " keypass 
if [ "$keypass" == "" ] ; then
keypass=$default_keypass
fi

read -p "Please enter Storepass[$default_storepass]: " storepass 
if [ "$storepass" == "" ] ; then
storepass=$default_storepass
fi

keytool -delete -alias $host -storepass $storepass  -keystore $JAVA_HOME/jre/lib/security/cacerts

echo yes|keytool -import -v -trustcacerts -alias $host  -file ~/$host.crt -keystore $JAVA_HOME/jre/lib/security/cacerts -keypass $keypass -storepass $storepass 

