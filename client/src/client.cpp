//
// Created by ruzhinsk@wincs.cs.bgu.ac.il on 05/01/2022.
//
#include "iostream"
#include "thread"
#include "boost/asio.hpp"
#include "../include/connectionHandler.h"
#include "../include/readFromSocketThread.h"
#include "../include/readFromKeyboardThread.h"

using namespace std;

int main (int argc, char *argv[]){
//    if(argc < 3){
//        std:: cerr << "Usage: " << argv[0] << "host port" << std::endl << std::endl;
//        return -1;
//    }
    std::string host = "127.0.0.1";
    short port = atoi("8888");
//    host = argv[1];
//    port = atoi(argv[2]);
    connectionHandler con(host,port);
    if(!con.connect()){
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    readFromKeyBoardThread rfk(con);
    readFromSocketThread rfs(con);

    thread keyboard(&readFromKeyBoardThread::run, &rfk);
    thread socket(&readFromSocketThread::run, &rfs);

    keyboard.join();
    socket.join();
    return 0;


}