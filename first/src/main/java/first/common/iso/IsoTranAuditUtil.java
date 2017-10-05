package first.common.iso;

import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IsoTranAuditUtil
{
    // 이배열을 틀리게 하면 SHUFFLE STRING KEY 값이 변한다 즉 로직의 변화가 일어 난다
    private static final char[]                UPER_CHARS         = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final int                   SID_LENGTH         = 20;
    private static final int                   TRANID_LENGTH      = 29;
    private static final int                   SHUFFLE_KEY_LENGTH = 13;
    private static final int                   RANDOM_LENGTH      = 3;
    private static final int                   ALLOW_ERR_CNT      = 5;
    private static final Map<String, String[]> tranInfoMap        = new ConcurrentHashMap<String, String[]>();

    public static boolean checkTransactionId(String userId, String sessionHashKey, String transactionID)
    {
        // 자릿수 체크
        if(transactionID.length() != TRANID_LENGTH)
        {
            System.out.println("transactionID is not 29 length ");
            return false;
        }

        // 이 부분은 에러메시지로 대신한다
        String stringShuffleKey = transactionID.substring(SID_LENGTH);
        String shuffledHashKey = transactionID.substring(0, SID_LENGTH);
        int[] numberShffleKey = StringToExchanges(stringShuffleKey);
        String tranTimeStr = "";

        for (int i : numberShffleKey)
        {
            tranTimeStr += i;
        }

        long tranTime = Long.parseLong(tranTimeStr.substring(RANDOM_LENGTH, SHUFFLE_KEY_LENGTH)); // 3자리까지는 의미없는 random 숫자
        long randomVa = Long.parseLong(tranTimeStr.substring(0, RANDOM_LENGTH));

        String deShuffle = deShuffleSid(shuffledHashKey, numberShffleKey);

        /*--------------------------------------------------------------------------------------------------------------
         * 트렌젝션 값을 조사함
         *------------------------------------------------------------------------------------------------------------*/

        String[] preTranInfo = tranInfoMap.get(userId);

        // 이전 트랜젝션 값이 없으면
        if (preTranInfo == null)
        {
            System.out.println("preTranInfo is null");

            preTranInfo = new String[] { transactionID, tranTime + "", "0" };
            tranInfoMap.put(userId, preTranInfo);

            return true;
        }

        // 이전값과 비교를 한다
        String preTranId = preTranInfo[0];
        String preTranTime = preTranInfo[1];
        String errorCnt = preTranInfo[2];

        System.out.println("preTranId : " + preTranId + " preTranTime : " + "errorCnt : " + errorCnt + "\n");

        // 이전 키와 비교 해서 같은면 무조건 false
        if (transactionID.equals(preTranId))
        {
            System.out.println("requestHashKey Equeal : preTranId !!!! tranWillbeBan! \n");

            return false;
        }

        // 세션에 있는 값과 틀리면 무조건 ban
        if (!sessionHashKey.equals(deShuffle))
        {
            System.out.println("sessionHashKey is not matche tranKey !! " + deShuffle);
            return false;
        }

        // 아래 시간은 조정가능 5번까지는 봐준다
        if (Long.parseLong(preTranTime) > tranTime)
        {
            int errCnt = Integer.parseInt(errorCnt);
            preTranInfo[2] = errCnt + 1 + "";

            System.out.println("preTranTime is not Less !! \n");

            // 5개가 연속으로 들어 올수는 없다
            if (errCnt >= ALLOW_ERR_CNT)
            {
                System.out.println("Five times input less Time occure Err !! \n");
                tranInfoMap.put(userId, preTranInfo);

                return false;
            }

            tranInfoMap.put(userId, new String[] { transactionID, tranTime + "", preTranInfo[2] });

            return true;
        }

        // 이상이 없을때는 아래와 같이 한다
        tranInfoMap.put(userId, new String[] { transactionID, tranTime + "", "0" });

        StringBuffer sb = new StringBuffer();

        sb.append("HashKey        : ").append(sessionHashKey).append("\n");
        sb.append("Random         : ").append(randomVa).append("\n");
        sb.append("Timestamp      : ").append(tranTime).append("\n");
        sb.append("NumberShffleKey: ").append(tranTimeStr).append("\n");
        sb.append("ShffledHashKey : ").append(shuffledHashKey).append("\n");
        sb.append("StringShffleKey: ").append(stringShuffleKey).append("\n");
        sb.append("TransactionID  : ").append(transactionID).append("\n");
        sb.append("ArrayShffleKey : ").append(numberShffleKey).append("\n");
        sb.append("deShuffleValue : ").append(deShuffle).append("\n");

        sb.append("DiffProcTime   : ").append(procTime(Long.parseLong(preTranTime), tranTime)).append("\n");

        System.out.println(sb.toString());

        return true;
    }

    public static String deShuffleSid(String shuffled, int[] exchanges)
    {
        int size = shuffled.length();
        char[] chars = shuffled.toCharArray();

        for (int i = 1; i < size; i++)
        {
            int n = exchanges[size - i - 1];
            char tmp = chars[i];
            chars[i] = chars[n];
            chars[n] = tmp;
        }
        return new String(chars);
    }

    public static String excelColumnCharsToNumber(String input) throws NumberFormatException
    {
        double result = new Double(0);

        input = input.toUpperCase(); // 대문자로 치환한다. 모든것을

        for (int ctr = 0; ctr < input.length(); ++ctr)
        {
            int position = getPosition(UPER_CHARS, input.charAt(ctr)) + 1;

            if (position > 0)
            {
                result += Math.pow(UPER_CHARS.length, input.length() - ctr - 1) * position;
            } else
            {
                throw new NumberFormatException(input + " is Not ExcelNumber !!");
            }
        }

        return numberToString(result);
    }

    public static int getPosition(char[] args, char chk)
    {
        int position = 0;
        if (args != null)
        {
            for (char comp : args)
            {
                if (comp == chk)
                {
                    return position;
                }
                position++;
            }
        }
        return Integer.MIN_VALUE; // 만일 찾는값이 없을때는 에러를 내게 만든다
    }

    public static String numberToString(Number input)
    {
        NumberFormat numFmt = NumberFormat.getInstance();
        numFmt.setGroupingUsed(false);
        return numFmt.format(input);
    }

    public static String procTime(long startTime, long endTime)
    {
        long millisecs = endTime - startTime;

        long secs = millisecs / 1000;
        long hour = secs / 60 / 60;
        long min = (secs - hour * 60 * 60) / 60;
        long sec = secs - hour * 60 * 60 - min * 60;
        long millisec = millisecs - (hour * 60 * 60 + min * 60 + sec) * 1000;
        String returnString = hour + "Hour " + min + "Min " + sec + "Sec " + millisec + "Ms";
        return returnString;
    }

    public static int[] StringToExchanges(String exStr)
    {
        String exchange = excelColumnCharsToNumber(exStr);

        int[] result = new int[SID_LENGTH - 1];

        for (int i = 0; i < SHUFFLE_KEY_LENGTH; i++)
        {
            result[i] = Integer.parseInt(exchange.substring(i, i + 1));
        }

        return result;
    }

    public static void main(String[] args)
    {
        String sessionHashKey = "wPYyZNJbJgFsKhnR8WDK";
        String userId = "YAMAYAMA";

        checkTransactionId(userId, sessionHashKey, "s8WZFJnbRgKhNKwyYDJPHZIFMZIUU");

        // 이후값 이후값 Test
        checkTransactionId(userId, sessionHashKey, "g8RJFJnsZbKhNWwDKPyYKAWEPLDJI");
        checkTransactionId(userId, sessionHashKey, "s8WZFJnbRgKhNKwyYDJPHZIFMZIUU"); // 1 이전값넣기
        checkTransactionId(userId, sessionHashKey, "s8JDFWnRgbKhNPwKYJZyPOAWSZDHZ"); // 2 이전값넣기
        checkTransactionId(userId, sessionHashKey, "s8JZFWnJRbKhNPwDKgyYKKVDVIMQB"); // 3 이전값넣기
        checkTransactionId(userId, sessionHashKey, "s8JZgWnFRbKhNDwKYJPyOCRTDJBBK"); // 4 이전값넣기
        checkTransactionId(userId, sessionHashKey, "s8gZFWnJRDKhNPwKYJbyQZKAIPFOG"); // 5 이전값넣기
        checkTransactionId(userId, sessionHashKey, "g8JZFJnsRDKhNPwyWKbYLZXKJXPCE"); // 6 이전값넣기 여기부터에러남

        // 같은값 테스트 두번같은 값을 보낼때
        checkTransactionId(userId, sessionHashKey, "sWRJFJn8KbKhNPwyDgYZTNJRYXSFF");
        checkTransactionId(userId, sessionHashKey, "sWRJFJn8KbKhNPwyDgYZTNJRYXSFF");

        // 변조값 테스트 값을 로직에 안맞게 생성했을때
        checkTransactionId(userId, sessionHashKey, "sWRJFJn8KbKhNPwyDgYCTNJRYXSFF");

        // 자릿수가 안맞을때
        checkTransactionId(userId, sessionHashKey, "sWRJFJn8KbKhNPwyDgYCTNJYXSFF");
    }
}
