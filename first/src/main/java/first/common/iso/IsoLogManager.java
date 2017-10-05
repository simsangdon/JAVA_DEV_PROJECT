package first.common.iso;

import java.util.LinkedHashMap;
import java.util.Map;

public class IsoLogManager
{

    private static String _START_LOG = "▷";
    private static String _END_LOG   = "◁";
    private static String _SEP_ELE   = "┃";
    private static String _SEP_VAL   = "☆";
    private static int    _KEY       = 0;
    private static int    _VALUE     = 1;

    public static Map<String, Object> logStringToMap(String input)
    {
        if(isEmpty(input))
        {
            return null;
        }

        String logStr = findBetweenStr(input, _START_LOG, _END_LOG, true);

        if(logStr == null)
        {
            return null;
        }

        String[] elements = logStr.split(_SEP_ELE);
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        for (String element : elements)
        {
            if (isEmpty(element))
            {
                continue;
            }

            String[] keyValue = element.split(_SEP_VAL);

            if (keyValue.length == 2)
            {
                map.put(keyValue[_KEY], keyValue[_VALUE]);
            }
        }

        return map;
    }

    public static final boolean isEmpty(String value)
    {
        if (value == null || value.isEmpty())
        {
            return true;
        }
        return false;
    }

    public static String mapToLogString(Map<String, Object> map)
    {
        if(map == null)
        {
            return _START_LOG + _END_LOG;
        }

        if(map.keySet().size() == 0)
        {
            return _START_LOG + _END_LOG;
        }

        StringBuilder sb = new StringBuilder(_START_LOG);
        String[] keys = map.keySet().toArray(new String[]{});

        for (int i = 0; i < keys.length; i++)
        {
            if (isNotEmpty(keys[i]))
            {
                sb.append(keys[i]);
                sb.append(_SEP_VAL);
                sb.append(map.get(keys[i]));
            }

            if(i + 1 < keys.length)
            {
                sb.append(_SEP_ELE);
            }
        }

        sb.append(_END_LOG);

        return sb.toString();
    }

    public static final boolean isNotEmpty(Object value)
    {
        return !isEmpty((String) value);
    }

    public static final String findBetweenStr(String input, String start, String end, boolean removeSide)
    {
        int startIndx = input.indexOf(start);

        if(startIndx == -1)
        {
            return null;
        }

        if(input.indexOf(end, startIndx + 1) == -1)
        {
            return null;
        }

        int endIndx = input.indexOf(end, startIndx + 1) + end.length(); //프롬인덱스는 시작보다 커야된다


        if(removeSide)
        {
            return input.substring(startIndx + start.length(), endIndx - end.length());
        } else
        {
            return input.substring(startIndx, endIndx);
        }
    }

    public static void main(String[] args)
    {
        Map<String, Object> map = logStringToMap("voidxx▷이름☆박성재┃성별☆남자┃나이☆사십삼세◁zzzz");

        System.out.print(mapToLogString(map));
    }
}
