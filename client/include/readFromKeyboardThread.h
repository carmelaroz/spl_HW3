//
// Created by ruzhinsk@wincs.cs.bgu.ac.il on 05/01/2022.
//

#ifndef CLIENT_READFROMKEYBOARDTHREAD_H
#define CLIENT_READFROMKEYBOARDTHREAD_H
#include "connectionHandler.h"



class readFromKeyBoardThread {
private:
    connectionHandler& con;
public:
    readFromKeyBoardThread(connectionHandler& c);
    void run();

};

#endif //CLIENT_READFROMKEYBOARDTHREAD_H