//
// Created by ruzhinsk@wincs.cs.bgu.ac.il on 05/01/2022.
//

#include "../include/readFromSocketThread.h"

readFromSocketThread::readFromSocketThread(connectionHandler &c) : con(c){}

void readFromSocketThread::run() {
    while (!con.shouldTerminate()){
        std::string ans = con.decoder();
        if(ans.empty()){
            std::cout << "Disconnected. Exiting...\n " << std::endl;
            break;
        }
        std::cout << ans << std::endl;
        if(ans == "ACK 03"){
            while (!con.shouldTerminate() && !con.isLogOutSent()){
            }
        if(con.shouldTerminate()){
            std::cout << "connection is closed" << std::endl;
            con.close();
        }
        }
    }


}