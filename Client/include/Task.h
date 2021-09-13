//
// Created by spl211 on 04/01/2021.
//

#ifndef CLIENT_TASK_H
#define CLIENT_TASK_H

#endif //CLIENT_TASK_H
#include <mutex>
#include <condition_variable>
#include "connectionHandler.h"
#include <thread>
#include <boost/asio.hpp>
using boost::asio::ip::tcp;

//each thread is given a task object, according to their id they know what to do when they run.
//id = 1 for thread1 to get input from the client and send it to the server
//id = 2 for thread2 to get the answer from the server

class Task {
private:
    int id;
    ConnectionHandler *connectionHandler;
    bool shouldTerminate = false; //change to "true" when thread1 send LOGOUT (opcode 4) and thread2 receive ACK.

public:
    static int answerop4; // 0/-1 - there is no answer, 1 - answer ERROR and should continue, 2 - answer is ACK and should terminate
    Task(int id, ConnectionHandler *cH);
    void run();
};