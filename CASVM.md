## Create a named instance in a vm ##
**NOTE**: Keep the server name under 15 characters otherwise it will be truncated in the Alfresco CIFS interface

In this example we are using the name `alfresco`

### Virtual Box ###

Set up a host network

In VirtualBox: File/Preferences/Host-Only Ethernet Adapter
> IPv4 Address: 192.168.56.1

In the VM:
Adapter 2:

Intel PRO/1000 MT Desktop (Host-only adapter, 'VirtualBox Host-Only Ethernet Adapter')

### Alfresco Ubuntu VM ###
On vm add to /etc/network/interfaces
```
auto eth1

iface eth1 inet static
        address 192.168.56.10
        netmask 255.255.255.0
```
```
/etc/init.d/networking restart
```


### On Host Machine ###

Add to hosts file
```
192.168.56.10	alfresco
```

## Setup Apache on Alfresco ##
We are going to use Apache proxying such that both the CAS url and the Alfresco url are to the alfresco box, but the CAS URL is actually proxied to the Chassis/CAS box.

```
a2enmod ssl
a2enmod rewrite
a2enmod proxy_http
a2ensite default-ssl
```
Edit /etc/apache2/sites-enabled/default-ssl

After the last /Directory add the following, where
cashost is the name your CAS/Chassis installation is known by:
```
        <Proxy *>
               Order Allow,Deny
               Allow from all
        </Proxy>

        # reverse proxy configuration for CAS
        <Location /sso>
                ProxyPass        http://cashost:8080/sso
                ProxyPassReverse http://cashost:8080/sso
        </Location>

        # reverse proxy configuration for alfresco 
        # to enable https urls 
        <Location /share>
                ProxyPass        http://localhost:8080/share
                ProxyPassReverse http://localhost:8080/share
        </Location>
        <Location /alfresco>
                ProxyPass        http://localhost:8080/alfresco
                ProxyPassReverse http://localhost:8080/alfresco
        </Location>
```

## Create Self-signed certificate on Alfresco machine ##

(This example is replacing the default Apache file you might like to call it something different)

The `ssleay.cnf` is used to ensure the CN in the certificate is the same as the hostname, though following command is interactive (use hostname alfresco)
```
sudo su
cd ~
cp /usr/share/ssl-cert/ssleay.cnf .
make-ssl-cert ssleay.cnf generate-default-snakeoil --force-overwrite
```

## Import the certificates to the Alfresco JVM ##

```
cd /opt/alfresco-3.4.d/java/jre/lib/security/
keytool -import -file /etc/ssl/certs/ssl-cert-snakeoil.pem -keystore ./cacerts
```

## Configure Alfresco ##

In /opt/alfresco/tomcat/conf/context.xml add
```
<WatchedResource>WEB-INF/lib/WWARN_Share_Code-1.0.jar</WatchedResource>
```

## Customize Alfresco ##
```
cd ~
./install.sh alfresco.well.ox.ac.uk alfresco.well.ox.ac.uk
```
## Flash upload problems with self signed certificate ##

https://bugbase.adobe.com/index.cfm?event=bug&id=2926796

https://issues.alfresco.com/jira/browse/ALF-1324

Linux workaround is to create content as text by hand.