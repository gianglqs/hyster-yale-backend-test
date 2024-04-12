#!/bin/sh

#
# Copyright (c) 2024. Hyster-Yale Group
# All rights reserved.
#

echo "Restarting Tomcat Server"
sudo service tomcat stop

pid=`ps aux | grep tomcat | grep -v grep | grep -v retomcat | awk '{print $2}'`
if [ -n "$pid" ]
   then
    {
      sudo kill -9 $pid
    }
fi

sudo service tomcat start

#Make Shell Script Executable
# $ sudo chmod +x /home/manage_tc.sh

#Create Cronjob to run the script every 5 minutes to restart tomcat if needed
#0 10 * * 6 /home/manage_tc.sh >/dev/null 2>&1
