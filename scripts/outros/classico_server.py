import socket
import struct
import sys
import os
import thread
import time

HOST = ''   # Symbolic name meaning all available interfaces
PORT = 10000 # Arbitrary non-privileged port
portAddr = 10000
ipAddr = ''

try :
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    print 'Socket created'
    s.bind((HOST, PORT))
    print 'Socket bind complete'
    newRequest = s.recvfrom(1024)
    nameFile = newRequest[0] #nome do arquivo video
    addr = newRequest[1] #Endereco do cliente
    ipAddr = addr[0]
    portAddr = addr[1]
except socket.error, msg :
    print 'Failed to create socket. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
    sys.exit()
finally:
    print >> sys.stderr, 'closing socket'
    s.close()

def timeAlert():
    passos = 60
    while(passos > 0):
        time.sleep(10)
        passos -= 10
        print passos,'seconds'

raw_input("Press Enter to continue...")
os.system("cd ../evalvid && ./client.sh &")
time.sleep(1)
thread.start_new_thread(timeAlert, ())
os.system("cd ../evalvid && ./mp4trace -f -s "+ipAddr+" 10000 sample.mp4 > files/st01")
time.sleep(1)
os.system("sudo kill -1 $(ps -C 'tcpdump' -o pid=)")



