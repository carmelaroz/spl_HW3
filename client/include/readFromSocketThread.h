//
// Created by ruzhinsk@wincs.cs.bgu.ac.il on 05/01/2022.
//

#ifndef CLIENT_READFROMSOCKETTHREAD_H
#define CLIENT_READFROMSOCKETTHREAD_H
#include "connectionHandler.h"

class readFromSocketThread {
private:
    connectionHandler& con;
public:
    readFromSocketThread(connectionHandler& c);
    void run();
};
#endif //CLIENT_READFROMSOCKETTHREAD_H
