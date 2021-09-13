//
// Created by spl211 on 25/12/2020.
//
#include <iostream>
#include <ostream>
#include <istream>
#include <sstream>
#include <string>
#include "../include/Task.h"


using namespace std;


int main (int argc, char *argv[]) {
    if (argc < 3) {
        cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    string host = argv[1];
    short port = atoi(argv[2]);
    ConnectionHandler ch(host, port);
    if (!ch.connect()) {
        return 1;
    }
    Task task1(1, &ch);
    Task task2(2, &ch);
    thread thread1(&Task::run, &task1); //thread1 to get input from the client and send it to the server
    task2.run();
    thread1.join();
    ch.close();
    return 0;
}

