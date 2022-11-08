CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude -Iboost_1_61_0
LDFLAGS:=-lboost_system -pthread -lboost_thread
SRCS = $(wildcard ./src/*.cpp)
OBJS = $(patsubst ./src/%.cpp,./bin/%.o, $(SRCS))
DEPS := $(patsubst %.o,%.d, $(OBJS))
all: EchoClient
	g++ -o bin/echo.elf bin/connectionHandler.o bin/client.o bin/readFromSocketThread.o bin/readFromKeyboardThread.o $(LDFLAGS)

EchoClient: bin/connectionHandler.o bin/client.o bin/readFromSocketThread.o bin/readFromKeyboardThread.o

bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/client.o: src/client.cpp
	g++ $(CFLAGS) -o bin/client.o src/client.cpp
bin/readFromKeyboardThread.o: src/readFromKeyboardThread.cpp
	g++ $(CFLAGS) -o bin/readFromKeyboardThread.o src/readFromKeyboardThread.cpp
bin/readFromSocketThread.o: src/readFromSocketThread.cpp
	g++ $(CFLAGS) -o bin/readFromSocketThread.o src/readFromSocketThread.cpp

.PHONY: clean
clean:
	rm -f bin/*