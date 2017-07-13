import java.util.Scanner;

/**
 * Created by USER on 2017-07-13.
 */
public class Test {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        // length 17
        String str = "11111111000100111";
        byte[] binaryBytes = new byte[str.length()%4!=0?str.length()/4+1:str.length()];
        int arrayIndex = -1;
        for(int i = 0 ; i < str.length(); i++){
            final int shift = i%8;
            if(shift == 0){
                arrayIndex++;
            }
            binaryBytes[arrayIndex] += ((str.charAt(i) - '0') << shift);
        }
        for (byte bt: binaryBytes) {
            System.out.println(bt&0xFF);
        }
    }
}
