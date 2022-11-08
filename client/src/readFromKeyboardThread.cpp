//
// Created by ruzhinsk@wincs.cs.bgu.ac.il on 05/01/2022.
//

#include "../include/readFromKeyboardThread.h"

readFromKeyBoardThread::readFromKeyBoardThread(connectionHandler& c) : con(c) {}

void readFromKeyBoardThread::run() {
    while (!con.shouldTerminate()){
        const short bufsize = 1024;
        char buf[bufsize];
        std:: cin.getline(buf,bufsize);
        std::string line(buf);
        if(!con.sendLine(line)){
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        if(line.substr(0,7) == "LOGOUT"){
            while (!con.shouldTerminate() && !con.isLogOutFailed()){

            }
            con.resetLogout();
        }
    }
}
