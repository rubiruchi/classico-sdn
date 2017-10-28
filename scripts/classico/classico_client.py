import socket
import struct
import sys
import subprocess
import os

host = '192.168.2.1'
port = 10000
videofile = "sample.mp4"

# os.system("ufw enable")

if len(sys.argv) > 2:
	videofile = sys.argv[2]
	# try:   
	# 	s1 = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	# 	s1.bind(('', 10001))
	# 	videofile, addr = s1.recvfrom(1024)
	# finally:
	#     s1.close()
    

try:
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(('', 10000))
    s.sendto(videofile, (host, port))
    os.system("echo "+sys.argv[1]+" $(date +'%F %T,%3N') ")
except socket.error:
    print 'Failed to create socket'
    sys.exit()
 
s.close()

script = "cd ../../evalvid &&"
script += "rm -rf files/sd"+sys.argv[1]+" &&"
# if len(sys.argv) <= 2:
script += "ufw enable &&"
script += "tcpdump -n -tt -v udp port 10000 >  files/sd"+sys.argv[1]
os.system(script)


