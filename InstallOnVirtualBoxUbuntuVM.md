# Introduction #



# Steps #

Set up a VirtualBox VM as per http://tim-pizey.blogspot.com/2011/08/alfresco-setup.html

Then, within the VM:

```

wget http://sourceforge.net/projects/alfresco/files/Alfresco%203.4.d%20Community/alfresco-community-sdk-3.4.d.zip

mkdir tmp

mv alfresco-community-sdk-3.4.d.zip tmp/

cd tmp
unzip alfresco-community-sdk-3.4.d.zip
cd ..

sudo su

mv tmp /opt/alfresco-community-sdk-3.4
cd /opt
ln -s alfresco-community-sdk-3.4 alfresco-community-sdk

```

```
cd dsn-chassis-ext
sudo su

apt-get install ant
apt-get install openjdk-6-jdk

/etc/init.d/afresco stop
ln -s /opt/alfresco-3.4.d /opt/alfresco

cp /opt/alfresco/tomcat/webapps/alfresco.war /opt/alfresco/tomcat/webapps/alfresco.war.bak

```

```

svn checkout https://dsn-chassis-ext.googlecode.com/svn/trunk/ dsn-chassis-ext --username Tim.Pizey@gmail.com
cd dsn-chassis-ext
cd scripts
sudo su
./install.sh

```


# Install with CAS integration #
Read http://ecmstuff.blogspot.com/2011/06/configuring-alfresco-for-sso-with-cas.html

Then [CASVM](http://code.google.com/p/dsn-chassis-ext/wiki/CASVM)

# Initialise studies #

Goto http://alfresco:8080/share/
login as admin/admin
http://alfresco/share/page/repository
Ensure that there is a directory WWARN containing a directory Studies


Add My Studies from http://alfresco:8080/share/page/customise-user-dashboard (Scroll down)

Now you should be good to go.