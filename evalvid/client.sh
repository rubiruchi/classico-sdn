#!/bin/bash
#
# Client
rm -f files/rd01 &&
ufw enable &&
tcpdump -i any -n -tt -v udp port 10000 > files/rd01 
