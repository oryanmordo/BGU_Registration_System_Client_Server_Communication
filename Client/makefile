CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude -lpthread -lboost_thread
LDFLAGS:=-lboost_system -lpthread -lboost_thread

all: BGRSclient
	g++ -o bin/BGRSclient bin/BGRSclient.o bin/connectionHandler.o bin/Task.o bin/messageEncoderDecoder.o $(LDFLAGS)

BGRSclient: bin/connectionHandler.o bin/Task.o bin/messageEncoderDecoder.o bin/BGRSclient.o

bin/BGRSclient.o: src/BGRSclient.cpp
	g++ $(CFLAGS) -o bin/BGRSclient.o src/BGRSclient.cpp

bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/Task.o: src/Task.cpp
	g++ $(CFLAGS) -o bin/Task.o src/Task.cpp

bin/messageEncoderDecoder.o: src/messageEncoderDecoder.cpp
	g++ $(CFLAGS) -o bin/messageEncoderDecoder.o src/messageEncoderDecoder.cpp


.PHONY: clean
clean:
	rm -f bin/*