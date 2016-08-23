#include <stdlib.h>
#include <boost/locale.hpp>
#include "connectionHandler.h"
#include "../encoder/utf8.h"
#include "../encoder/encoder.h"
#include <boost/thread/thread.hpp>

void runRead(ConnectionHandler *connectionHandler){
	while (1) {
		const short bufsize = 1024;
		char buf[bufsize];
		std::string line(buf);
		int len=line.length();
		std::string answer;
		if (!(*connectionHandler).getLine(answer)) {
			std::cout << "Disconnected. Exiting...\n" << std::endl;
			break;
		}
		len=answer.length();
		answer.resize(len-1);
		std::cout << "Reply: " << answer << " " <<  std::endl << std::endl;
		if (answer == "SYSMSG QUIT ACCEPTED.") {
			std::cout << "Exiting...\n" << std::endl;
			break;
		}
	}
}
void runWrite(ConnectionHandler *connectionHandler){
	while (1) {
		const short bufsize = 1024;
		char buf[bufsize];
		std::cin.getline(buf, bufsize);
		std::string line(buf);
		if (!connectionHandler->sendLine(line)) {
			std::cout << "Disconnected. Exiting...\n" << std::endl;
			break;
		}
	}
}
int main (int argc, char *argv[]) {
	if (argc < 3) {
		std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
		return -1;
	}
	std::string host = argv[1];
	short port = atoi(argv[2]);

	ConnectionHandler connectionHandler(host, port);
	if (!connectionHandler.connect()) {
		std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
		return 1;
	}

	boost::thread writeThread= boost::thread(runWrite,(&connectionHandler));
	boost::thread readThread= boost::thread(runRead,(&connectionHandler));

	readThread.join();

	return 0;
}
