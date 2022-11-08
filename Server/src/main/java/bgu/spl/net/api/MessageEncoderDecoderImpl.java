package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder {
    private byte[] bytes = new byte[1 << 10];
    private int len = 0;
    @Override
    public Object decodeNextByte(byte nextByte) {
        if (nextByte == ';') {
            return popString();
        }
        pushByte(nextByte);
        return null; //not a line yet
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }
    @Override
    public byte[] encode(Object message) {
        byte[] ans = new byte[1 << 16];

        if(message.toString().substring(0,4).equals("1007") || message.toString().substring(0,4).equals("1008")){
            ans[0] = (byte) message.toString().charAt(0);
            ans[1] = (byte) message.toString().charAt(1);
            ans[2] = (byte) message.toString().charAt(2);
            ans[3] = (byte) message.toString().charAt(3);
            int i = 4;
            message = message.toString().substring(4);
            Short age;
            Short numOfFollowing;
            Short numOfFollowers;
            Short numOfPosts;
            String[] users = message.toString().split("\n");

            for(String s : users){
                String[] content = s.split(" ");
                age = Short.parseShort(content[0]);
                numOfPosts = Short.parseShort(content[1]);
                numOfFollowers = Short.parseShort(content[2]);
                numOfFollowing = Short.parseShort(content[3]);
                ans[i++] = shortToBytes(age)[0];
                ans[i++] = shortToBytes(age)[1];
                ans[i++] = shortToBytes(numOfPosts)[0];
                ans[i++] = shortToBytes(numOfPosts)[1];
                ans[i++] = shortToBytes(numOfFollowers)[0];
                ans[i++] = shortToBytes(numOfFollowers)[1];
                ans[i++] = shortToBytes(numOfFollowing)[0];
                ans[i++] = shortToBytes(numOfFollowing)[1];
            }
            ans[i++] = ';';
        }
        else {
            message = message.toString() + ";";
            ans = message.toString().getBytes(StandardCharsets.UTF_8);
        }
        return ans;
    }

    private String popString() {
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }

    private byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
