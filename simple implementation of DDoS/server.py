import socket
import argparse
from threading import Thread

socketList = []
'''
command format:
#-H xxx.xxx.xxx.xxx -p xxxx -c <start|stop>
'''

def main():
	global socketList
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	s.bind(('0.0.0.0',58883))
	s.listen(1024)
	# new thread to accept connection requests
	t = Thread(target=waitConnect, args=(s,))
	t.start()

	print ('Wait at least a client connected.')
    
	while len(socketList)==0:
		pass
	print ('Connection dected.')
	while True:
		print ('=' * 50)
		print ('The command format:"#-H xxx.xxx.xxx.xxx -p xxxx -c <start|stop>"')
		# wait for input
		cmd_str = raw_input('Please input cmd:')

		if len(cmd_str):
			if cmd_str[0] == '#':
				sendCmd(cmd_str)

# waiting for connections
def waitConnect(s):
	global socketList
	while True:
		sock,addr = s.accept()
		if sock not in socketList:
			socketList.append(sock)

# send command
def sendCmd(cmd):
	global socketList
	print ('Send command......')
	for sock in socketList:
		sock.send(cmd.encode('utf-8'))

if __name__ == '__main__':
	main()




