#include "../include/connectionHandler.h"
 
using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;
 
connectionHandler::connectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_), terminate(false), logoutSent(false), logoutFailed(false){}
    
connectionHandler::~connectionHandler() {
    close();
}
 
bool connectionHandler::connect() {
    std::cout << "Starting connect to " 
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool connectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool connectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool connectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, ';');
}

bool connectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, ';');
}
 
bool connectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character. 
    // Notice that the null character is not appended to the frame string.
    try {
		do{
			getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool connectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
    std::vector<char> encodedStr = Encoder(frame);
    int size = encodedStr.size();
    char send[size];
    for(int i = 0; i < size; i++){
        send[i] = encodedStr.at(i);
    }
    return sendBytes(send,size);
//	bool result=sendBytes(frame.c_str(),frame.length());
//	if(!result) return false;
//	return sendBytes(&delimiter,1);
}
 
// Close down the connection properly.
bool connectionHandler::close() {
    try{
        socket_.close();
        return true;
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
        return false;
    }
}

 std::vector<char> connectionHandler::Encoder(std::string msg) {
    unsigned int i = 0;
    string opCode = "";
    std::vector<char> ans;
    while(i < msg.length() && msg.at(i) != ' ') {
        opCode += msg.at(i);
        i++;
    }
    i++;
    if(opCode == "REGISTER") {
        ans.push_back('0');
        ans.push_back('1');
//        ans[0] = '0';
//        ans[1] = '1';
        while (i < msg.length()) {
            if(msg.at(i) == ' ') {
                ans.push_back('\0');
                i++;
            }
            else {
                ans.push_back(msg.at(i));
                i++;
            }
        }
        ans.push_back('\0');
        i++;
        ans.push_back(';');
        i++;
    }
    else if(opCode == "LOGIN") {
        ans.push_back('0');
        ans.push_back('2');
        while (i < msg.length()) {
            if(msg.at(i) == ' ') {
                ans.push_back('\0');
                i++;
            }
            else {
                ans.push_back(msg.at(i));
                i++;
            }
        }
        ans.push_back(';');
    }
    else if(opCode == "LOGOUT") {
        ans.push_back('0');
        ans.push_back('3');
        ans.push_back(';');
        logoutSent = true;
    }
    else if(opCode == "FOLLOW") {
        ans.push_back('0');
        ans.push_back('4');
        while (i < msg.length()) {
            if((msg.at(i) != ' ') ) {
                ans.push_back(msg.at(i));
                i++;
            }
            else {
                i++;
            }
        }
        ans.push_back('\0');
        ans.push_back(';');
    }
    else if(opCode == "POST") {
        ans.push_back('0');
        ans.push_back('5');
        while (i < msg.length()) {
            ans.push_back(msg.at(i));
            i++;
        }
        ans.push_back('\0');
        ans.push_back(';');
    }
    else if(opCode == "PM") {
        ans.push_back('0');
        ans.push_back('6');
        while (i < msg.length()) {
            if(msg.at(i) == ' ') {
                ans.push_back(' ');
                i++;
            }
            else {
                ans.push_back(msg.at(i));
                i++;
            }
        }
        ans.push_back('\0');
        ans.push_back(';');
    }
    else if(opCode == "LOGSTAT") {
        ans.push_back('0');
        ans.push_back('7');
        ans.push_back(';');
    }
    else if(opCode == "STAT") {
        ans.push_back('0');
        ans.push_back('8');
        while (i < msg.length()) {
            ans.push_back(msg.at(i));
            i++;
        }
        ans.push_back('\0');
        ans.push_back(';');
    }
    else if(opCode == "BLOCK") {
        ans.push_back('1');
        ans.push_back('2');
        while (i < msg.length()) {
            ans.push_back(msg.at(i));
            i++;
        }
        ans.push_back('\0');
        ans.push_back(';');
    }
    return ans;
}

std::string connectionHandler::decoder() {
    std::string answer = "";
    char opCode[2];
    if(!getBytes(opCode,2)) {
        std::cout << "connection was interrupted" << std::endl;
        return NULL;
    }
    if(opCode[0] == '0' && opCode[1] == '9'){
        answer += "NOTIFICATION ";
        answer += notificationDecoder();
        if(!getBytes(opCode,1)) {
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }
        return answer;
    }
    else if(opCode[0] == '1' && opCode[1] == '0'){
        answer += "ACK ";
        answer += ackDecoder();
        if(!getBytes(opCode,1)) {
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }
        return answer;
    }
    else if(opCode[0] == '1' && opCode[1] == '1'){
        answer += "ERROR ";
        answer += errorDecoder();
        if(!getBytes(opCode,1)) {
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }
        return answer;
    }
    return NULL;
}

std::string connectionHandler::notificationDecoder(){
    std::string ans = "";
    char stat[1];
    std::string postingUser = "";
    std::string  content = "";
    if(!getBytes(stat,1)){
        std::cout << "connection was interrupted" << std::endl;
        return NULL;
    }
    if(stat[0] == '1'){
        ans += "Public ";
    } else{
        ans += "PM ";
    }
    if(!getFrameAscii(postingUser,'\0')){
        std::cout << "connection was interrupted" << std::endl;
        return NULL;
    }
    postingUser = postingUser.substr(0,postingUser.length()-1);
    ans += postingUser + " ";
    if(!getFrameAscii(content,'\0')){
        std::cout << "connection was interrupted" << std::endl;
        return NULL;
    }
    content = content.substr(0, content.length()-1);
    ans += content;
    return ans;
}

std::string connectionHandler::ackDecoder(){
    std::string answer = "";
    std::string userName = "";
    char messageOpcode[2];
    if(!getBytes(messageOpcode,2)) {
        std::cout << "connection was interrupted" << std::endl;
        return NULL;
    }
    answer += messageOpcode;
    answer += " ";
    if((messageOpcode[0] == '0') & (messageOpcode[1] == '3')) {
        terminate = true;
    }
    if((messageOpcode[0] == '0') & (messageOpcode[1] == '4')) {
        if(!getBytes(messageOpcode,2)) {
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }
        answer += messageOpcode;
        answer += " ";
        if(!getFrameAscii(userName,'\0')){
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }
        userName = userName.substr(0, userName.length()-1);
        answer += userName;
        if(!getBytes(messageOpcode,1)) {
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }

    }
    else if((messageOpcode[0] == '0') & (messageOpcode[1] == '7') || (messageOpcode[0] == '0') & (messageOpcode[1] == '8')) {
        char argument[2];
        if(!getBytes(argument,2)) {
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }
        short age = bytesToShort(argument);
        answer += std::to_string(age);
        answer += " ";
        if(!getBytes(argument,2)) {
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }
        short numOfPosts = bytesToShort(argument);
        answer += std::to_string(numOfPosts);
        answer += " ";
        if(!getBytes(argument,2)) {
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }
        short numOfFollowers = bytesToShort(argument);
        answer += std::to_string(numOfFollowers);
        answer += " ";
        if(!getBytes(argument,2)) {
            std::cout << "connection was interrupted" << std::endl;
            return NULL;
        }
        short numOfFollowing = bytesToShort(argument);
        answer += std::to_string(numOfFollowing);
    }

    return answer;

}

std::string connectionHandler::errorDecoder(){
    std::string messageOpcode = "";
//    if(!getFrameAscii(messageOpcode,';')){
//        std::cout << "connection was interrupted" << std::endl;
//        return NULL;
//    }
    char arg[2];
    if(!getBytes(arg,2)) {
        std::cout << "connection was interrupted" << std::endl;
        return NULL;
    }
    if((arg[0] == '0') & (arg[1] == '3')) {
        logoutFailed = true;
    }
//    if(messageOpcode[0] == '0' & messageOpcode[1] == '3') {
//        logoutFailed = true;
//    }
    messageOpcode += arg[0];
    messageOpcode += arg[1];

    return messageOpcode;
}

short connectionHandler::bytesToShort(char* bytesArr) {
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

bool connectionHandler:: shouldTerminate(){
    return terminate;
}
bool connectionHandler::isLogOutSent(){
    return logoutSent;
}

bool connectionHandler:: isLogOutFailed(){
    return logoutFailed;
}

void connectionHandler::resetLogout(){
    if(logoutSent)
        logoutSent = false;
    if(logoutFailed)
        logoutFailed = false;
}


