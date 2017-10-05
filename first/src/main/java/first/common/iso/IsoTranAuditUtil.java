package first.common.iso;

import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IsoTranAuditUtil
{
    // �̹迭�� Ʋ���� �ϸ� SHUFFLE STRING KEY ���� ���Ѵ� �� ������ ��ȭ�� �Ͼ� ����
    private static final char[]                UPER_CHARS         = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final int                   SID_LENGTH         = 20;
    private static final int                   TRANID_LENGTH      = 29;
    private static final int                   SHUFFLE_KEY_LENGTH = 13;
    private static final int                   RANDOM_LENGTH      = 3;
    private static final int                   ALLOW_ERR_CNT      = 5;
    private static final Map<String, String[]> tranInfoMap        = new ConcurrentHashMap<String, String[]>();

    public static boolean checkTransactionId(String userId, String sessionHashKey, String transactionID)
    {
        // �ڸ��� üũ
        if(transactionID.length() != TRANID_LENGTH)
        {
            System.out.println("transactionID is not 29 length ");
            return false;
        }

        // �� �κ��� �����޽����� ����Ѵ�
        String stringShuffleKey = transactionID.substring(SID_LENGTH);
        String shuffledHashKey = transactionID.substring(0, SID_LENGTH);
        int[] numberShffleKey = StringToExchanges(stringShuffleKey);
        String tranTimeStr = "";

        for (int i : numberShffleKey)
        {
            tranTimeStr += i;
        }

        long tranTime = Long.parseLong(tranTimeStr.substring(RANDOM_LENGTH, SHUFFLE_KEY_LENGTH)); // 3�ڸ������� �ǹ̾��� random ����
        long randomVa = Long.parseLong(tranTimeStr.substring(0, RANDOM_LENGTH));

        String deShuffle = deShuffleSid(shuffledHashKey, numberShffleKey);

        /*--------------------------------------------------------------------------------------------------------------
         * Ʈ������ ���� ������
         *------------------------------------------------------------------------------------------------------------*/

        String[] preTranInfo = tranInfoMap.get(userId);

        // ���� Ʈ������ ���� ������
        if (preTranInfo == null)
        {
            System.out.println("preTranInfo is null");

            preTranInfo = new String[] { transactionID, tranTime + "", "0" };
            tranInfoMap.put(userId, preTranInfo);

            return true;
        }

        // �������� �񱳸� �Ѵ�
        String preTranId = preTranInfo[0];
        String preTranTime = preTranInfo[1];
        String errorCnt = preTranInfo[2];

        System.out.println("preTranId : " + preTranId + " preTranTime : " + "errorCnt : " + errorCnt + "\n");

        // ���� Ű�� �� �ؼ� ������ ������ false
        if (transactionID.equals(preTranId))
        {
            System.out.println("requestHashKey Equeal : preTranId !!!! tranWillbeBan! \n");

            return false;
        }

        // ���ǿ� �ִ� ���� Ʋ���� ������ ban
        if (!sessionHashKey.equals(deShuffle))
        {
            System.out.println("sessionHashKey is not matche tranKey !! " + deShuffle);
            return false;
        }

        // �Ʒ� �ð��� �������� 5�������� ���ش�
        if (Long.parseLong(preTranTime) > tranTime)
        {
            int errCnt = Integer.parseInt(errorCnt);
            preTranInfo[2] = errCnt + 1 + "";

            System.out.println("preTranTime is not Less !! \n");

            // 5���� �������� ��� �ü��� ����
            if (errCnt >= ALLOW_ERR_CNT)
            {
                System.out.println("Five times input less Time occure Err !! \n");
                tranInfoMap.put(userId, preTranInfo);

                return false;
            }

            tranInfoMap.put(userId, new String[] { transactionID, tranTime + "", preTranInfo[2] });

            return true;
        }

        // �̻��� �������� �Ʒ��� ���� �Ѵ�
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

        input = input.toUpperCase(); // �빮�ڷ� ġȯ�Ѵ�. ������

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
        return Integer.MIN_VALUE; // ���� ã�°��� �������� ������ ���� �����
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

        // ���İ� ���İ� Test
        checkTransactionId(userId, sessionHashKey, "g8RJFJnsZbKhNWwDKPyYKAWEPLDJI");
        checkTransactionId(userId, sessionHashKey, "s8WZFJnbRgKhNKwyYDJPHZIFMZIUU"); // 1 �������ֱ�
        checkTransactionId(userId, sessionHashKey, "s8JDFWnRgbKhNPwKYJZyPOAWSZDHZ"); // 2 �������ֱ�
        checkTransactionId(userId, sessionHashKey, "s8JZFWnJRbKhNPwDKgyYKKVDVIMQB"); // 3 �������ֱ�
        checkTransactionId(userId, sessionHashKey, "s8JZgWnFRbKhNDwKYJPyOCRTDJBBK"); // 4 �������ֱ�
        checkTransactionId(userId, sessionHashKey, "s8gZFWnJRDKhNPwKYJbyQZKAIPFOG"); // 5 �������ֱ�
        checkTransactionId(userId, sessionHashKey, "g8JZFJnsRDKhNPwyWKbYLZXKJXPCE"); // 6 �������ֱ� ������Ϳ�����

        // ������ �׽�Ʈ �ι����� ���� ������
        checkTransactionId(userId, sessionHashKey, "sWRJFJn8KbKhNPwyDgYZTNJRYXSFF");
        checkTransactionId(userId, sessionHashKey, "sWRJFJn8KbKhNPwyDgYZTNJRYXSFF");

        // ������ �׽�Ʈ ���� ������ �ȸ°� ����������
        checkTransactionId(userId, sessionHashKey, "sWRJFJn8KbKhNPwyDgYCTNJRYXSFF");

        // �ڸ����� �ȸ�����
        checkTransactionId(userId, sessionHashKey, "sWRJFJn8KbKhNPwyDgYCTNJYXSFF");
    }
}
