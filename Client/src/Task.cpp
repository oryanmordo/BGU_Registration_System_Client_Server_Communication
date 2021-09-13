//
// Created by spl211 on 04/01/2021.
//

#include "../include/Task.h"


int Task::answerop4 = -1;  // 0/-1 - there is no answer, 1 - answer ERROR and should continue, 2 - answer is ACK and should terminate

Task::Task(int id, ConnectionHandler *cH) : id(id), connectionHandler(cH), shouldTerminate(false) {}

void Task::run() {
    if (id == 2) this_thread::sleep_for(std::chrono::microseconds(1000));
    while (!this->shouldTerminate) {
        if (id == 1) {  //id = 1 for thread1 to get input from the client and send it to the server
            string st;
            std::getline(cin, st);
            char array[1024];
            int num = connectionHandler->encode(st, array);
            char msg[num];
            for (int i = 0; i < num; i++) {
                msg[i] = array[i];
            }
            connectionHandler->sendBytes(msg, num);
            if (connectionHandler->encdec.getOpcode() == 4) {
                while (answerop4 != 1 && answerop4 != 2) {
                    this_thread::sleep_for(std::chrono::microseconds(15));
                }
                if (answerop4 == 1) answerop4 = -1;
                if (answerop4 == 2) {
                    shouldTerminate = true;
                }
            }
        }
        else { //(id == 2) id = 2 for thread2 to get the answer from the server
            char answer[1024];
            string ans;
            ans = connectionHandler->decode(answer); //print the server's answer
            if (ans[0] == '4')
                answerop4 = 1;
            else if (ans == "ACK 4" || ans == "0")
            {
                answerop4 = 2;
                shouldTerminate = true;
            }
        }
    }
}
