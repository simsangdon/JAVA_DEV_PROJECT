package first.common.iso;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Types;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsoDataBaseExcute
{
    private static String           INSERT           = "INSERT";
    private static String           SELECT           = "SELECT";
    private static String           UPDATE           = "UPDATE";
    private static String           DELETE           = "DELETE";
    private static String           DDL              = "DDL";
    private static String           DML              = "DML";
    private static String           PROCEDURE        = "PROCEDURE";
    private static String           UNKNOWN          = "UNKNOWN";
    private static String           DERBY            = "DERBY";
    private static String           ORACLE           = "ORACLE";
    private static String           POSTGRES         = "POSTGRES";
    private static String           SYBASE           = "SYBASE";
    private static String           MSSQL            = "MSSQL";
    private static String           MYSQL            = "MYSQL";
    private static String           DB2              = "DB2";
    private static String           TERADATA         = "TERADATA";
    private static String           JDBC             = "JDBC";
    private static String           EXCEL            = "EXCEL";
    private static final String     REX_PARAM_PATTEN = "#\\{?([a-zA-Z0-9_ ]+)\\}?#?";
    private static final String     REX_JAVA_COMMENT = "//[^\r\n]*";
    private static final String     REX_SQL_COMMENT  = "--[^\r\n]*";
    private static final String     REX_NEW_LINE     = "\r\n|\n\r|\r|\n";
    private static final String     NEW_LINE         = "\r\n";
    private static int              BUFFER_SIZE      = 1024 * 8;
    private static final char[]     UPER_CHARS       = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final BigInteger uperCharsLength  = BigInteger.valueOf(UPER_CHARS.length);

    /**
     * <PRE>
     *  ?‘?‡°?”?‘œï¿½ï¿½?…½ë»¾ï¿½?’•?–. SqlVo ï¿½ë¨¯ê½? ï§â‘¤ë±? ï¿½ë¨®?œ­ï¿½ï¿½åª›ï¿½?’«ï¿½ï¿½?‘?‡°?”?‘œï¿½ï¿½ëº¤ì £ ï¿½ì’•?–.
     *  ï¿½ã…½ë»¾ï¿½?„?’— ?‘?‡°?”ï¿½ë¨¯ê½Œï¿½ï¿½äºŒ?‡±ê½ï¿½ï¿½ï¿½?’–?‡… ï¿½ì„?? ; åª›ìˆˆï¿? ï¿½ë¨®?œ­ ï¿½ï¿½åª›ï¿½?’«ï¿½ï¿½ï¿½ëˆ?’—
     *  ?‡¾ëª„ì˜„ï¿½ï¿½ï¿½ê¹†?“£ ï¿½ì’–?‡…ï¿½ì’•?–.
     * </PRE>
     *
     * @location org.noru.db.core.DataBaseExcuter.java
     * @cdate 2013. 8. 26.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param conn
     * @param sqlVo
     * @param params
     * @param processer
     * @return
     * @throws SQLException
     */
    public static List<Map<String, Object>> excute(Connection conn, String query, Map<String, ? extends Object> params, int limittedCnt) throws SQLException
    {
        // ï¿½ê¾¨Ğ¢ï¿½ï¿½ï¿½ê³Œê»ï¿½ï¿½ï¿½?†?“£ï¿½ëš®?’— null è«›ì„‘?†š
        if (conn == null)
        {
            throw new SQLException("Connection is null !!");
        }
        // ï¿½ã…½ë»¾ï¿½ï¿½voåª›ï¿½ï¿½ë†?“£ï¿½ëš®ë£? null è«›ì„‘?†š
        if (query == null)
        {
            throw new SQLException("query is null !!");
        }

        long procStartTime = System.currentTimeMillis(); // ï§£ì„?”ï¿½ì’“ì»? ?•°?’•? °

        List<Map<String, Object>> result = null;

        String queryType = getQueryType(query);

        boolean successYn = false;
        int processedCount = 0;

        if (queryType == UNKNOWN)
        {
            throw new SQLException("SqlVo Type is Unknown check your sql !!");
        } else if (queryType == SELECT)
        {
            result = excuteSelect(conn, query, params, limittedCnt);
        } else if (queryType == PROCEDURE)
        {
            result = excuteProcedure(conn, query, params);
        } else if (queryType == DDL)
        {
            result = new ArrayList<Map<String, Object>>();

            successYn = excuteDdl(conn, query, params);
            if (successYn)
            {
                Map<String, Object> map = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
                map.put("SUCCESS_YN", successYn);
                result.add(map);
            }
        } else if (queryType == INSERT || queryType == UPDATE || queryType == DELETE)
        {
            processedCount = excuteDml(conn, query, params);
            if (processedCount > 0)
            {
                result = new ArrayList<Map<String, Object>>();
                Map<String, Object> map = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
                map.put("PROCESSED_COUNT", processedCount);
                result.add(map);
            }
        }

        printQuery(query, params, procStartTime);

        return result;
    }

    private static void printQuery(String query, Map<String, ? extends Object> params, long procStartTime)
    {
        try
        {
            StringBuilder sb = new StringBuilder();

            sb.append(query).append(NEW_LINE);

            sb.append(procTime(procStartTime)).append(NEW_LINE);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * <PRE>
     *  ?®ê¾©ï¿½ï¿½ë‰?’ªæ¿¡ì’–ì­…ï¿½ï¿½å«„ëªƒâ”› ï¿½ì’“ì»? ï§¥â‰ª? ™ï¿½ì„ë¦? ï¿½ê¾¨?˜’ ï§£ì„?† ?‚„ë¶¾ëµ«ï¿½ï¿½ï¿½ëŒï¼œï§ï¿½ï¿½?’•?–.
     *  EX)
     *  long procStartTime = System.currentTimeMillis();
     *  ...
     *  CommonUtil.procTime(procStartTime);
     * </PRE>
     *
     * @optitle
     * @cdate 2012. 3. 26.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param procStartTime
     * @return
     * @throws ClassNotFoundException
     */
    private static final String procTime(long procStartTime)
    {
        return procTime(procStartTime, System.currentTimeMillis());
    }

    private static final String procTime(long startTime, long endTime)
    {
        long millisecs = endTime - startTime;

        long secs = millisecs / 1000;

        long day = secs / 60 / 60 / 24;
        long hour = (secs - day * 60 * 60 * 24) / 60 / 60;
        long min = (secs - day * 60 * 60 * 24 - hour * 60 * 60) / 60;
        long sec = secs - day * 60 * 60 * 24 - hour * 60 * 60 - min * 60;

        long millisec = millisecs - (hour * 60 * 60 + min * 60 + sec) * 1000;
        String returnString = day + "day " + hour + "Hour " + min + "Min " + sec + "Sec " + millisec + "Ms";
        return returnString;
    }

    /**
     * <PRE>
     * INSERT, UPDATE, DELETE ?‡¾ëª„ì“£ ï¿½ã…½ë»¾ï¿½?’—ê¶“ï¿½ï¿?
     * </PRE>
     *
     * @location org.noru.db.core.DataBaseExcuter.java
     * @cdate 2013. 8. 26.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param conn
     * @param sqlVo
     * @param params
     * @return
     * @throws SQLException
     */
    private static int excuteDml(Connection conn, String query, Map<String, ? extends Object> params) throws SQLException
    {
        PreparedStatement pstmt = getPreparedStatement(conn, query, params, DML);
        int resultNum = pstmt.executeUpdate();
        pstmt.close();

        return resultNum;
    }

    /**
     * <PRE>
     * 1. DDL(Data Definition Language) ï¿½ê³—?” ï¿½ê³—ï¿? æ´¹ï¿½?´?Šâ?œç‘œï¿½ï¿½ëº¤ì“½ï¿½â‘¸?•²ï¿½ï¿½
     *    1) CREATE   : ï¿½ê³—?” ï¿½ê³•ì¿‹ï¿½?Œ?’ª åª›ì•¹ê»œç‘œï¿½ï¿½?•¹ê½?ï¿½â‘¸?•²ï¿½ï¿½
     *    2) DROP     : ï¿½ï¿½?” ï¿½ê³•ì¿‹ï¿½?Œ?’ª åª›ì•¹ê»œç‘œï¿½ï¿½ï¿½ì £ï¿½â‘¸?•²ï¿½ï¿½
     *    3) ALTER    : æ¹²ê³—?ˆï¿½ï¿½è?°ëŒ?˜±ï¿½ì„?’— ï¿½ê³—?” ï¿½ê³•ì¿‹ï¿½?Œ?’ª åª›ì•¹ê»œç‘œï¿½ï¿½?…¼?–† ï¿½ëº¤?“½ï¿½ì„?’— ï¿½ï¿½ë¸·ï¿½ï¿½ï¿½?‘¸?•²ï¿½ï¿½
     *    4) RENAME   : ï¿½ê³—?” ï¿½ê³•ì¿‹ï¿½?Œ?’ªï¿½ï¿½?ŒÑ‰ì†ï§ë‚†?“£ è¹‚ï¿½ê¼ï¿½?‘¸?•²ï¿½ï¿½
     *    5) TRUNCATE : ï¿½ëš¯?” ?‡‰ë¶¿ì“£ ï§¤ì’–?¹ ï¿½ì•¹ê½?ï¿½ï¿½?¥?‡ë¦°ï¿½ê³¹ê¹­æ¿¡ï¿½ï§ëš®ë±¾ï§ï¿? ROLLBACKï¿½ï¿½?º?‡ï¿½ï¿½Î½ë¹?ï¿½ëˆ?–.
     * 2. DCL(Data Control Language) ï¿½ê³—?” ï¿½ê³•ì¿‹ï¿½?Œ?’ª ï¿½ÑŠìŠœï¿½ë¨¯?“½ æ²…ëš°ë¸³ï¿½ï¿½ï¿½?’–ë¼±ï¿½?‘¸?•²ï¿½ï¿½
     *    1) GRANT : ï¿½ê³—?” ï¿½ê³•ì¿‹ï¿½?Œ?’ª åª›ì•¹ê»œï¿½ï¿½æ²…?š°ë¸³ï¿½ï¿½éºï¿½ë¿¬ï¿½â‘¸?•²ï¿½ï¿½
     *    2) REVOKE : ï¿½ë?ï¿? ?ºï¿½ë¿¬ï¿½ï¿½ï¿½ê³—?” ï¿½ê³•ì¿‹ï¿½?Œ?’ª åª›ì•¹ê»œï¿½ï¿½æ²…?š°ë¸³ï¿½ï¿½ç—?‘¥?ƒ¼ï¿½â‘¸?•²ï¿½ï¿½
     * </PRE>
     *
     * @location org.noru.db.core.DataBaseExcuter.java
     * @cdate 2013. 8. 27.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param conn
     * @param sqlVo
     * @param params
     * @return
     * @throws SQLException
     */
    private static boolean excuteDdl(Connection conn, String query, Map<String, ? extends Object> params) throws SQLException
    {
        PreparedStatement pstmt = getPreparedStatement(conn, query, params, DDL);
        pstmt.execute();
        pstmt.close(); // ?”±?Šëƒ¼ï¿½ã…»ï¿? ï¿½ãƒ¬ë¸? ä»¥ï¿½?–

        return true;
    }

    private static List<Map<String, Object>> excuteProcedure(Connection conn, String query, Map<String, ? extends Object> params) throws SQLException
    {
        List<Map<String, Object>> result = null;

        ResultSet resultSet = null;

        PreparedStatement pstmt = getPreparedStatement(conn, query, params, PROCEDURE);

        Map<String, Object> outPut = new LinkedHashMap<String, Object>();

        /*--------------------------------------------------------------------------------------------------------------
         * ï¿½ê¾¨ì¤ˆï¿½?’–ï¿½ï¿½ëªƒëœ² ResultSet ï¿½ï¿½è«›ì„‘?†šï¿½ì„?’—å¯ƒìŒ?Š¦ rsåª›ï¿½nullï¿½ï¿½ï¿½ê¾¨?•²ï¿½ï¿½
         *------------------------------------------------------------------------------------------------------------*/
        if (pstmt.execute())
        {
            resultSet = pstmt.getResultSet();
        }

        List<String> varList = new ArrayList<String>();
        setQuery(query, varList);

        /*--------------------------------------------------------------------------------------------------------------
         * ï¿½ê¾¨ì¤ˆï¿½?’–ï¿½ï¿½ï¿½ï¿½ëª„ë‹”æ¿¡ï¿½ï¿½ì„êº¼ä»¥ï¿½åª›ë¯©ë±¾ï¿½ï¿½ï¿½ï¿½ë¸? ï§£ì„?”?‘œï¿½ï¿½?Œï¿½ï¿½ï¿?
         *------------------------------------------------------------------------------------------------------------*/
        for (int i = 0; i < varList.size(); i++)
        {
            try
            {
                outPut.put(varList.get(i), ((CallableStatement) pstmt).getObject(i + 1));
            } catch (Exception e)
            {
                outPut.put(varList.get(i), null);
            }
        }

        /*--------------------------------------------------------------------------------------------------------------
         * ï§ëš¯?”ª ResultSet ï¿½ï¿½ï¿½ëˆ?’— å¯ƒìŒ?Š¦ï¿½ï¿½ï¿½ë??–¦ï¿½ì„?’— å¯ƒê³Œ?‚µ?‘œï¿½ï¿½?‚´ë¼±ï¿½?‘¤?–.
         *------------------------------------------------------------------------------------------------------------*/
        if (resultSet != null)
        {
            result = rsToObjectMapList(resultSet);
        } else if (outPut.size() > 0)
        {
            result = new ArrayList<Map<String, Object>>();
            result.add(outPut);
        }

        /*--------------------------------------------------------------------------------------------------------------
         * ?”±?Šëƒ¼ï¿½ã…»ï¿? ï¿½ãƒ¬ë¸? ä»¥ï¿½?–
         *------------------------------------------------------------------------------------------------------------*/
        pstmt.close();

        return result;
    }

    private static List<Map<String, Object>> excuteSelect(Connection conn, String query, Map<String, ? extends Object> params, int limittedCnt) throws SQLException
    {
        List<Map<String, Object>> result = null;

        PreparedStatement pstmt = getPreparedStatement(conn, query, params, SELECT);
        ResultSet resultSet = pstmt.executeQuery();
        result = rsToObjectMapList(resultSet, limittedCnt);

        pstmt.close();

        return result;
    }

    private static final List<Map<String, Object>> rsToObjectMapList(ResultSet resultSet) throws SQLException
    {
        return rsToObjectMapList(resultSet, 0);
    }

    /**
     * <PRE>
     * ResultSet ï¿½ï¿½List<Map<String, Object>> ï¿½ëº¥ê¹?æ¿¡ï¿½ï§ëš®ë±ºï¿½ï¿? ï¿½ë?ë¸? ï¿½ã…»ï¿? ï¿½ï¿½Ğ¦ï¿½ë¨®ì¤? ï§â‘¤ëª? ï§ëš®ë±ºï¿½ï¿?
     * </PRE>
     *
     * @location org.noru.database.DataBaseUtil.java
     * @cdate 2014. 4. 16.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param resultSet
     * @param uppercaseKey
     * @return
     * @throws SQLException
     */
    private static final List<Map<String, Object>> rsToObjectMapList(ResultSet resultSet, int limittedCnt) throws SQLException
    {
        if (resultSet == null)
        {
            return null;
        }

        List<Map<String, Object>> resultList = null;
        int procCnt = 0;

        ResultSetMetaData metaData = resultSet.getMetaData();
        int cCnt = metaData.getColumnCount();

        String[] keys = new String[cCnt];

        // ?ŒÑ‰ì†ï¿½â‘¥? è«›ì„?‚¬ ï¿½ì„?ˆƒï¿½ï¿½ï§ë“­?“£ ï§ëš®ë±¾ï¿½ï¿½ä»¥ï¿½ë–.
        for (int i = 1; i <= cCnt; i++)
        {
            keys[i - 1] = metaData.getColumnName(i).trim().toUpperCase();
        }

        while (resultSet.next())
        {
            if (resultList == null)
            {
                resultList = new ArrayList<Map<String, Object>>();
            }
            Map<String, Object> map = rsToMap(resultSet, keys);
            resultList.add(map);
            procCnt++;
            if (procCnt == limittedCnt)
            {
                return resultList;
            }
        }

        resultSet.close();

        return resultList; // å¯ƒê³Œ?‚µ?‘œï¿½è«›?„‘?†šï¿½ì’•?–.
    }

    private static final Map<String, Object> rsToMap(ResultSet resultSet, String keys[]) throws SQLException
    {
        if (resultSet == null)
        {
            return null;
        }
        // ï§ë?ï¿½ç‘œï¿½ï¿½?‚´ë¼? ï¿½â‘¤?–
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cCnt = metaData.getColumnCount();

        // ?ŒÑ‰ì†ï¿½â‘¥? è«›ì„?‚¬ ï¿½ì„?ˆƒï¿½ï¿½ï§ë“­?“£ ï§ëš®ë±¾ï¿½ï¿½ä»¥ï¿½ë–.
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 1; i <= cCnt; i++)
        {
            // ï¿½ã…»ï¿? ï§ëš®ë±¾ï¿½ï¿½ä»¥ï¿½ë–
            String key;
            if (keys == null || cCnt - 1 != keys.length)
            {
                key = metaData.getColumnName(i).trim().toUpperCase();
            } else
            {
                key = keys[i];
            }

            // ï§ëš¯?”ª ä»¥ë¬ì»™ï¿½ï¿½é®ê¾©ë?ï¿½ì‡°?ˆƒ Defaultæ¿¡ï¿½A, B, C, D, E ï¿½ï¿½åª›ìˆˆï¿? Excel æ¹²ê³—ï¿½ï¿½ï¿½è?ŒÑ‰ì†ï§ë‚†?“£ ï¿½ë??–
            if (isEmpty(key))
            {
                key = numberToExcelColumnChars(i);
            }

            Object value = columnDataToObject(metaData.getColumnType(i), resultSet, i);
            map.put(key, value);
        }
        return map;
    }

    /**
     * <PRE>
     *  ï¿½ãƒ¬?˜„?‘œï¿½ï¿½ë¬’ï¿½?ŒÑ‰ì† ï¿½ë?ì««ï¿½?‡°ì¤? è¹‚ï¿½?†•ï¿½ì’—ê¶“ï¿½ï¿?
     *  1ï¿½ï¿½A æ¿¡ï¿½è¹‚ï¿½?†š 2?‘œï¿½Bæ¿¡ï¿½è¹‚ï¿½?†š
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param input
     * @return
     */
    private static final String numberToExcelColumnChars(Number input)
    {
        return numberToExcelColumnChars(new BigInteger(numberToString(input)));
    }

    private static String numberToString(Number input)
    {
        NumberFormat numFmt = NumberFormat.getInstance();
        numFmt.setGroupingUsed(false);
        return numFmt.format(input);
    }

    /**
     * <PRE>
     *  ?‡¾?Œ„ë¸³ï¿½ï¿½ï¿½ê³—ë‹”ï¿½ï¿½ï§£ì„?” ï¿½ï¿½ï¿½ï¿½ï¿½ë‡ì¾? è¹‚ï¿½ê¼ï¿½ï¿?
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @Method numberToExcelColumnChars
     * @cdate 2015. 9. 16. ï¿½ã…½?‘ 3:05:58
     * @version 1.0
     * @author Administrator
     * @param input
     * @return
     */
    private static final String numberToExcelColumnChars(BigInteger input)
    {
        String rtnStr = "";

        BigInteger time = BigInteger.ZERO; // ï§ï¿½
        int rest = 0; // ï¿½ì„?‰§ï§ï¿½

        while (input.compareTo(uperCharsLength) == 1) // input > UPER_CHARS.length
        { // 26è¹‚ë??– ï¿½Ñ‰ãˆƒ

            time = input.divide(uperCharsLength); // ï§ï¿½
            rest = input.remainder(uperCharsLength).intValue(); // ï¿½ì„?‰§ï§ï¿½

            // CommonUtil.print(time, rest);
            if (time.compareTo(uperCharsLength.add(BigInteger.ONE)) > -1)
            { // ï§â‘¹?”  27è¹‚ë??– ï¿½Ñ‰ãˆƒ
                if (rest == 0)
                { // ï¿½ì„?‰§ï§ï¿½ï¿? ï¿½ë†?‘ï§ï¿½ï¿½ì„?‰§ï§ï¿½?’— Zæ¿¡ï¿½ç§»ì„‘?†šï¿½ëŒï¿½ï¿½ï¿?
                    rtnStr = UPER_CHARS[UPER_CHARS.length - 1] + rtnStr; // ï¿½ì„?‰§ï§ï¿½ï¿½ï½Œë¼±äºŒ?‡¨??
                    input = time.subtract(BigInteger.ONE); // Zæ¿¡ï¿½ç§»ì„‘?†šï¿½ï¿½å¯ƒê»‹?“£ ï§ï¿½ï§â‘¹?“£ ï¿½ì„êº? ä»¥ï¿½?–.
                } else
                {
                    rtnStr = UPER_CHARS[rest - 1] + rtnStr; // ï¿½ì„?‰§ï§ï¿½ì­? ï¿½ï½Œë¼? äºŒì‡¨?? ï¿½ã…¼?“¬ï¿½ï¿½?¨ê¾©ê¶?
                    input = time; // ï§â‘¹?“£ ï¿½ì„êº? ä»¥ï¿½?–.
                }
            } else
            { // 26 ï¿½ëŒ„ë¸?
                if (rest == 0)
                { // ï¿½ì„?‰§ï§ï¿½ï¿? ï¿½ë†?‘ï§ï¿½ï¿½ì„?‰§ï§ï¿½?’— Zæ¿¡ï¿½ç§»ì„‘?†šï¿½ëŒï¿½ï¿½ï¿?
                    rtnStr = UPER_CHARS[UPER_CHARS.length - 1] + rtnStr; // ï¿½ì„?‰§ï§ï¿½ï¿½ï½Œë¼±äºŒ?‡¨??
                    rtnStr = UPER_CHARS[time.subtract(BigInteger.valueOf(2)).intValue()] + rtnStr; // Zæ¿¡ï¿½ç§»ì„‘?†šï¿½ï¿½åª›ë?ªï¿½ ?®?‡±ï¼œæ?¨ï¿½ï§â‘¸ë£„æ´¹ëªƒê¹· ï§£ì„?” ï¿½ëŒì¨?
                } else
                {
                    rtnStr = UPER_CHARS[rest - 1] + rtnStr; // ï¿½ì„?‰§ï§ï¿½ï¿½ï½Œë¼±äºŒ?‡¨??
                    rtnStr = UPER_CHARS[time.subtract(BigInteger.ONE).intValue()] + rtnStr; // ï§â‘¸ë£„æ´¹ëªƒê¹· ï§£ì„?” ï¿½ëŒì¨?
                }

                input = BigInteger.ZERO;
            }
        }

        if (input.compareTo(BigInteger.ZERO) == 1)
        { // 0ï¿½ì‡°ë¸? ï§â‘¤ëª? ï§£ì„?” ï¿½ì’“ì¾¬ï¿½??ï¿½æ¿¡ï¿½ï¿½?Œï¼œï§ï¿½ï¿½?”…?’—ï¿½ï¿½
            rtnStr += UPER_CHARS[input.subtract(BigInteger.ONE).intValue()]; // 26è¹‚ë??–åª›ìˆ†êµ…ï¿½ï¿½ï¿½ë¬’ï¿½å¯ƒê»‹?“£ ï§£ì„?” ï¿½ëŒï¿½ï¿½ï¿?
        }

        return rtnStr;
    }

    /**
     * ï¿½ë‚…? °ï¿½ï¿½åª›ë?ªì”  null ï¿½ë¨®?’— null String ï¿½ï¿½å¯ƒìŒ?Š¦ true?‘œï¿½return ï¿½ì’•?–.
     *
     * <pre>
     *
     * [ï¿½ÑŠìŠœ ï¿½ë‰? £]
     *
     * isEmpty("")      ===> true
     * isEmpty(null)    ===> true
     * isEmpty("1")     ===> false
     *
     * </pre>
     *
     * @param value
     * @return boolean
     */
    private static final boolean isEmpty(String value)
    {
        if (value == null || value.isEmpty())
        {
            return true;
        }
        return false;
    }

    /**
     * <PRE>
     *  rsï¿½ï¿½ï¿½ê³—?” ï¿½ê³•ï¿? objectæ¿¡ì’•ï¿½å¯ƒ?Œ?–†ï¿½â‘¤?–.
     * </PRE>
     *
     * @location org.noru.converter.ConverterDatabase.java
     * @cdate 2013. 9. 4.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param type
     * @param rs
     * @param colNum
     * @return
     */
    private static final Object columnDataToObject(int type, ResultSet rs, int colNum)
    {
        try
        {
            switch (type)
            {
            case Types.BIT: // -7
            {
                return rs.getBoolean(colNum);
            }
            case Types.TINYINT: // -6
            case Types.SMALLINT:// 5
            {
                return rs.getShort(colNum);
            }
            case Types.INTEGER: // 4
            {
                return rs.getInt(colNum);
            }
            case Types.FLOAT: // 6
            case Types.REAL: // 7
            {
                return rs.getFloat(colNum);
            }
            case Types.DOUBLE: // 8
            {
                return rs.getDouble(colNum);
            }
            case Types.BIGINT: // -5
            {
                return rs.getLong(colNum);
            }
            case Types.NUMERIC: // 2
            case Types.DECIMAL: // 3
            {
                return rs.getBigDecimal(colNum);
                // return rs.getString(colNum);
            }
            case Types.CHAR: // 1
            case Types.VARCHAR: // 12
            case Types.NCHAR: // = -15;
            case Types.NVARCHAR: // = -9;
            {
                return rs.getString(colNum);
            }
            case Types.LONGNVARCHAR: // = -16;
            case Types.LONGVARCHAR: // -1
            {
                return longVarcharToString(rs.getCharacterStream(colNum)); // ï¿½ã…½?“ƒ?”±?‡±?“£ ï¿½ìŒë¼±ï¿½ï¿?..
            }
            case Types.DATE: // 91
            {
                return rs.getDate(colNum);
            }
            case Types.TIME: // 92
            {
                return rs.getTime(colNum);
            }
            case Types.TIMESTAMP: // 93
            {
                return rs.getTimestamp(colNum);
            }
            case Types.BINARY: // -2
            case Types.VARBINARY: // -3
            case Types.LONGVARBINARY: // -4
            {
                return rs.getBytes(colNum);
            }
            case Types.CLOB:// 2005
            {
                return clobToString(rs.getClob(colNum));
            }
            case Types.NCLOB: // 2011
            {
                return nClobToString(rs.getNClob(colNum));
            }
            case Types.BLOB: // 2004
            {
                return blobToBytes(rs.getBlob(colNum)); // Blobï¿½ï¿½byte[]æ¿¡ï¿½è«›ì„‘?†šï¿½ì’•?–.
            }
            case Types.ARRAY: // 2003
            {
                return arrayToStringArray(rs.getArray(colNum)); // String[] ï¿½ëº¥ê¹?æ¿¡ï¿½è«›ì„‘?†šï¿½ì’•?–.
            }
            case Types.BOOLEAN: // 16
            {
                return rs.getBoolean(colNum);
            }
            case Types.NULL: // 0
            {
                return null;
            }
            case Types.ROWID: // -8
            {
                return rowIdToString(rs.getRowId(colNum)); // ï¿½ê³´ï¿½ï¿½ï¿½ï¿½?…¿?–.
            }
            default:
                return rs.getObject(colNum);
            }
        } catch (Exception e)
        {
            // void
        }

        try
        {
            return rs.getString(colNum);

        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private static String[] arrayToStringArray(Array input) throws SQLException
    {
        return (String[]) input.getArray();
    }

    private static byte[] blobToBytes(Blob input) throws SQLException
    {
        if (input == null)
        {
            return null;
        }
        int length = (int) input.length();
        byte[] body = input.getBytes(1, length);
        return body;
    }

    private static String clobToString(Clob input) throws SQLException
    {
        if (input == null)
        {
            return "";
        }
        long length = input.length();
        String body = input.getSubString(1, (int) length);
        return body;
    }

    private static String longVarcharToString(Reader reader) throws IOException
    {
        String data = "";
        try
        {
            if (reader != null)
            {
                // ï¿½ìŒë¼±ï¿½ï¿½ï¿½?…½?“ƒ?”±?‡±?“£ ï¿½ï¿½?˜£ï¿½ï¿½è¸°ê¾ª?
                StringBuffer buff = new StringBuffer();
                char[] ch = new char[BUFFER_SIZE];
                int len = -1;
                while ((len = reader.read(ch)) != -1)
                {
                    buff.append(ch, 0, len);
                }
                // ï¿½ï¿½?˜£ï¿½ï¿½è¸°ê¾ª??‘œï¿½String æ¿¡ï¿½è¹‚ï¿½?†š
                data = buff.toString();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
        return data;
    }

    private static String nClobToString(NClob input) throws SQLException, IOException
    {
        if (input == null)
        {
            return "";
        }
        Reader reader = input.getCharacterStream();
        StringBuffer buff = new StringBuffer(); // ï¿½ìŒë¼±ï¿½ï¿½ï¿½?…½?“ƒ?”±?‡±?“£ ï¿½ï¿½?˜£ï¿½ï¿½è¸°ê¾ª?
        char[] ch = new char[BUFFER_SIZE];
        int len = -1;
        while ((len = reader.read(ch)) != -1)
        {
            buff.append(ch, 0, len);
        }
        // ï¿½ï¿½?˜£ï¿½ï¿½è¸°ê¾ª??‘œï¿½String æ¿¡ï¿½è¹‚ï¿½?†š
        String data = buff.toString();
        return data;
    }

    private static String rowIdToString(RowId input)
    {
        return new String(input.getBytes());
    }

    private static final PreparedStatement getPreparedStatement(Connection conn, String query, Map<String, ? extends Object> params, String queryType) throws SQLException
    {
        PreparedStatement pstmt;

        List<String> varList = new ArrayList<String>();

        query = setQuery(query, varList);

        if (queryType == PROCEDURE)
        {
            CallableStatement cstmt = conn.prepareCall(query);
            for (int i = 0; i < varList.size(); i++)
            {
                Object value = getParamValue(params, varList.get(i));
                if (value == null)
                {
                    cstmt.registerOutParameter(i + 1, java.sql.Types.VARCHAR);
                } else if (value instanceof CharSequence)
                {
                    cstmt.registerOutParameter(i + 1, java.sql.Types.VARCHAR);
                } else if (value instanceof Number)
                {
                    cstmt.registerOutParameter(i + 1, java.sql.Types.NUMERIC);
                }
            }
            pstmt = cstmt;
        } else if (queryType == SELECT)
        {
            // ï¿½ï¿½?›¾ï¿½ì„?œ²ï¿½ëŒ„ê½£ç‘œï¿½ï§£?„?”ï¿½ì„ë¦? ï¿½ê¾ªë¸? ï¿½ë—®?˜¿

            pstmt = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            String dbType = getDataBaseType(conn);

            if (dbType == MYSQL)
            {
                pstmt.setFetchSize(Integer.MIN_VALUE);
                conn.setAutoCommit(false);
            } else if (dbType == POSTGRES)
            {
                pstmt.setFetchSize(50);
                conn.setAutoCommit(false);
            } else if (dbType == EXCEL)
            {

            } else
            {
                // FetchSize åª›ï¿½8096, rowï¿½ì„ï¿? 400ï§ëš­ì»? åª›ì’–?”ªï¿½ï¿½postgresï¿½ë¨¯ê½? 34?¥ï¿½ï¿½ëº£ë£„ï¿½ï¿½fatchï¿½ì’“ì»™ï¿½ï¿½å«„ëªƒâ”
                pstmt.setFetchSize(BUFFER_SIZE);
                conn.setAutoCommit(false);
            }
        } else
        {
            // insert update ï¿½ï¿½æ´¹ëªƒê¹? ?Œã…»ì» ï¿½?’—ê¶“ï¿½ï¿?
            conn.setAutoCommit(true);
            pstmt = conn.prepareStatement(query);
        }
        for (int i = 0; i < varList.size(); i++)
        {
            pstmt.setObject(i + 1, getParamValue(params, varList.get(i)));
        }

        return pstmt;
    }

    /**
     * <PRE>
     * ï¿½ê¾¨?˜’ï¿½ï¿½åª›ìˆˆ?”  ï¿½ë?ì««ï¿½?‡°ì¤? ï§¡ì–œ?’—ï¿½ï¿½
     * DERBY    , Apache Derby
     * DB2      , DB2
     * DB2ZOS   , DB2ZOS
     * HSQL     , HSQL Database Engine
     * SQLSERVER, Microsoft SQL Server
     * MYSQL    , MySQL
     * ORACLE   , Oracle
     * POSTGRES , PostgreSQL
     * SYBASE   , Sybase
     * </PRE>
     *
     * @location org.noru.database.DataBaseUtil.java
     * @cdate 2013. 9. 24.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param conn
     * @return
     */
    private static final String getDataBaseType(Connection conn)
    {
        String dataBaseType = JDBC;
        try
        {
            String productName = conn.getMetaData().getDatabaseProductName().toLowerCase();

            if (productName.contains("derby"))
            {
                dataBaseType = DERBY;
            } else if (productName.contains("oracle"))
            {
                dataBaseType = ORACLE;
            } else if (productName.contains("postgresql"))
            {
                dataBaseType = POSTGRES;
            } else if (productName.contains("sybase"))
            {
                dataBaseType = SYBASE;
            } else if (productName.contains("microsoft"))
            {
                dataBaseType = MSSQL;
            } else if (productName.contains("mysql"))
            {
                dataBaseType = MYSQL;
            } else if (productName.contains("db2"))
            {
                dataBaseType = DB2;
            } else if (productName.contains("teradata"))
            {
                dataBaseType = TERADATA;
            } else if (productName.contains("excel"))
            {
                dataBaseType = EXCEL;
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return dataBaseType;
    }

    /**
     * <PRE>
     * ï¿½ì„ì¨·ï¿½ï¿½è¹‚ï¿½ê¼ï¿½ï¿½ï¿½ì¢?‹”ï¿½ë‰?‘èª˜ï¿½ì¤? ï¿½ê¾¨?˜’ï¿½ï¿½åª›ìˆˆ?”  map utile ï¿½ë¨®ê½”ï¿½ï¿½ï¿½ë¶¾ë–.
     * </PRE>
     *
     * @location org.noru.database.DataBaseUtil.java
     * @cdate 2013. 9. 25.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param params
     * @param param
     * @return
     */
    private static final Object getParamValue(Map<String, ? extends Object> params, String key)
    {
        return params.get(key);
    }

    /**
     * <PRE>
     * ï¿½ã…»?”ï§ï¿½ê¼? ?‘?‡°?”?‘œï¿½PreparedStatementï¿½ï¿½ï¿½ê³¹ë¹?ï¿½ì„ì¾? è¹‚ï¿½ê¼ï¿½?’•?–.
     * </PRE>
     *
     * @location org.noru.database.SqlVo.java
     * @cdate 2013. 9. 23.
     * @version 1.0
     * @author è«›ëº¤ê½?ï¿½ï¿½
     * @tag
     * @param query
     */
    private static String setQuery(CharSequence query, List<String> varList)
    {
        String originQuery = query.toString();

        varList = new ArrayList<String>();

        /*--------------------------------------------------------------------------------------------------------------
         * ï¿½ã…½ë»¾ï¿½?Ÿ’ueryï¿½ë¨¯ê½Œï¿½?…»ìªŸåª›ï¿½ï¿½ì¢ë‹” ï¿½ëˆ?’—å¯ƒê»‹ï¿? ï¿½ê¾¨?˜’ ï§£ì„?† è¹‚ï¿½ê¼?
         *------------------------------------------------------------------------------------------------------------*/
        String preparedStatementQuery = originQuery.replace("\t", "    ").replace("\r\n", "\n").replace("\r", "\n").trim();

        /*--------------------------------------------------------------------------------------------------------------
         * ï¿½ã…½ë»¾ï¿½?Ÿ’ueryï¿½ï¿½ï§â‘¤ë±? Comment?‘œï¿½ï¿½?’“êµ? æ´¹ëª…?‘ ; ?‡¾ëª„ì˜£ï¿½ï¿½ï¿½ì’–?‡… æ´¹ëªƒ?”?¨ï¿½?®ï¿½lineï¿½ï¿½ï§ï¿½?™ä»¥ï¿½?–.
         *------------------------------------------------------------------------------------------------------------*/
        preparedStatementQuery = removeVoidLine(removeSqlComment(preparedStatementQuery).replace(";", ""));

        /*--------------------------------------------------------------------------------------------------------------
         * PreparedStatement ?‘œï¿½ï§£?„?” ï¿½ì„ë¦? ï¿½ê¾ªë¹? #([a-zA-Z0-9_- ]+)# ï¿½â‘¦ê½©ï¿½ï¿½ï¿½?‘¥ë¼±ç‘œï¿??æ¿¡ì’•ï¿½å¯ƒï¿?
         *------------------------------------------------------------------------------------------------------------*/
        Pattern ptmsPattern = Pattern.compile(REX_PARAM_PATTEN);
        Matcher ptmsMatcher = ptmsPattern.matcher(preparedStatementQuery);
        while (ptmsMatcher.find())
        {
            varList.add(ptmsMatcher.group(1).trim());
        }

        /*--------------------------------------------------------------------------------------------------------------
         * è¹‚ï¿½?‹”?‘œï¿½ï§?‘¤ëª? ?æ¿¡ï¿½è¹‚ï¿½ê¼ï¿½?’•?– ?‘?‡°?”?‘œï¿½ï¿½ï¿½ì—¯ï¿½ï¿½å¯ƒê³—? ™ï¿½ì’•?–.
         *------------------------------------------------------------------------------------------------------------*/
        preparedStatementQuery = preparedStatementQuery.replaceAll(REX_PARAM_PATTEN, "?");

        return preparedStatementQuery;
    }

    private static final String removeSqlComment(String input)
    {
        return removeComment(input, false);
    }

    /**
     * <PRE>
     *  ï¿½ì’–ì¨? äºŒì‡±ê½? --, // æ¿¡ï¿½ï¿½ì’–?˜‰ï¿½ëŒê½? NEW_LINE æºëš¯ï¿½ï§ï¿½æ´¹ëªƒâ”?¨ï¿½NEW_LINEï¿½ì‡°ì¤? ï¿½ï¿½ê»?
     *  ï¿½Ñ‰ìœ­ä»¥ï¿½äºŒì‡±ê½ï¿½ï¿½å¯ƒ?Œ?Š¦ ï¿½ê¾¨?˜’?‘œï¿½ï¿½ê³•ãˆƒ 800 ï¿½ë¨­ï¿? ï¿½ì„ë¼±åª›ï¿½è‡¾ëª„ì˜£ï¿½ë¨¯ê½? ï¿½ë¨®?œ­åª›ï¿½ï¿½ì’•?–.
     *  input = input.replaceAll(REX_MULTILINE_COMMENT, "");
     *
     * </PRE>
     *
     * @optitle
     * @cdate 2013. 8. 20.
     * @version 1.0
     * @author Administrator
     * @tag
     * @param input
     * @param isJava
     * @return
     */
    private static final String removeComment(String input, boolean isJava)
    {
        for (; input.indexOf("/*") > -1;)
        {
            int startIndx = input.indexOf("/*");
            int endIndx = input.indexOf("*/", startIndx) + 2; // ï¿½ê¾¨ï¼¼ï¿½ëªƒëœ³ï¿½ã…»?’— ï¿½ì’–?˜‰è¹‚ë??– ?Œã…¼ë¹ï¿½?’•?–

            if (startIndx < endIndx)
            {
                input = input.replace(input.substring(startIndx, endIndx), "");
            } else
            {
                break;
            }
        }
        if (isJava)
        {
            // ï¿½ê¾¨?˜’ ?‡¾ëª„ì˜£ ï¿½ê¾§?‰´ï§ï¿½ì­? åª›ê¾¨?–.
            input = input.replaceAll(REX_JAVA_COMMENT, "");
        } else
        {
            input = input.replaceAll(REX_SQL_COMMENT, "");
        }
        return input;
    }

    private static final String removeVoidLine(String input)
    {
        StringBuilder sb = new StringBuilder();
        String[] result = input.split(REX_NEW_LINE);
        for (String line : result)
        {
            if (isNotEmpty(line.trim()))
            {
                sb.append(rtrim(line)).append(NEW_LINE);
            }
        }
        return sb.toString();
    }

    private static final String rtrim(String s)
    {
        int i = s.length() - 1;
        while (i > 0 && Character.isWhitespace(s.charAt(i)))
        {
            i--;
        }
        return s.substring(0, i + 1);
    }

    private static final boolean isNotEmpty(Object value)
    {
        return !isEmpty((String) value);
    }

    private static String getQueryType(String query)
    {
        query = query.trim();
        String firstWord = "";

        /*--------------------------------------------------------------------------------------------------------------
          ?‡¾ëª„ì˜£ï¿½ï¿½ï¿½ê³—?˜˜?”±?‰ë’— ï¿½ì’“ì»™ï¿½ï¿½ï¿½?…»?˜’ å«„ëªƒ?”èª˜ï¿½ì¤? ï¿½ì„?”ªï¿½ï¿½?®ê¾§íƒ³?‘œï¿½ï¿½?’•?–.
        --------------------------------------------------------------------------------------------------------------*/
        if (query.length() > 15)
        {
            firstWord = query.substring(0, 15).split("[^A-Za-z0-9]", 2)[0];
        } else
        {
            firstWord = query.split("[^A-Za-z0-9]", 2)[0];
        }

        if ("SELECT".equalsIgnoreCase(firstWord))
        {
            return SELECT;
        } else if ("UPDATE".equalsIgnoreCase(firstWord))
        {
            return UPDATE;
        } else if ("INSERT".equalsIgnoreCase(firstWord))
        {
            return INSERT;
        } else if ("DELETE".equalsIgnoreCase(firstWord))
        {
            return DELETE;
        } else if ("EXEC".equalsIgnoreCase(firstWord))
        {
            return PROCEDURE;
        } else if ("CREATE".equalsIgnoreCase(firstWord))
        {
            return DDL;
        } else if ("DROP".equalsIgnoreCase(firstWord))
        {
            return DDL;
        } else if ("ALTER".equalsIgnoreCase(firstWord))
        {
            return DDL;
        } else if ("COMMENT".equalsIgnoreCase(firstWord))
        {
            return DDL;
        } else if ("RENAME".equalsIgnoreCase(firstWord))
        {
            return DDL;
        } else if ("TRUNCATE".equalsIgnoreCase(firstWord))
        {
            return DDL;
        } else if ("MERGE".equalsIgnoreCase(firstWord))
        {
            return UPDATE;
        } else if (query.toUpperCase().matches("^\\{\\s.*CALL.*\\}"))
        {
            return PROCEDURE;
        } else if ("WITH".equalsIgnoreCase(firstWord))
        {
            return SELECT;
        } else
        {
            return UNKNOWN;
        }
    }

    /**
     * <PRE>
     *
     *  åª›ï¿½Databaseè¹‚ï¿½connection ?‡¾ëª„ì˜£
     *
     *  Connection conn = getConnection("org.postgresql.Driver"                        , "jdbc:postgresql://localhost:5432/yama"                               , "yama"     , "playboy4");
     *  Connection conn = getConnection("oracle.jdbc.driver.OracleDriver"              , "jdbc:oracle:thin:@localhost:1521/XE"                                 , "hr"       , "playboy4");
     *  Connection conn = getConnection("org.apache.derby.jdbc.EmbeddedDriver"         , "jdbc:derby:C:/DevEnv64/workspace/YUP_WEB/YAMA_EMBEDED;create=true"   , "hr"       , "yama"    );
     *  Connection conn = getConnection("com.ibm.db2.jcc.DB2Driver"                    , "jdbc:db2://147.6.119.117:50000/edwid"                                , "bidwusr1" , "yama"    );
     *  Connection conn = getConnection("com.microsoft.sqlserver.jdbc.SQLServerDriver" , "jdbc:sqlserver://localhost:1433;databaseName=yama"                   , "bidwusr1" , "yama"    );
     *  Connection conn = getConnection("com.sybase.jdbc3.jdbc.SybDriver"              , "jdbc:sybase:Tds:127.0.0.1:2638/iqdemo"                               , "DBA"      , "sql"     );
     *  Connection conn = getConnection("com.teradata.jdbc.TeraDriver"                 , "jdbc:teradata://10.220.16.33/TMODE=ANSI,CHARSET=UTF8"                , "BDW_INDI" , "new1234" );
     *  Connection conn = getConnection("com.mysql.jdbc.Driver"                        , "jdbc:mysql://127.0.0.1:3306/test?zeroDateTimeBehavior=convertToNull" , "yama"     , "new1234" );
     *  Connection conn = getConnection("org.sqlite.JDBC"                              , "jdbc:sqlite:C:/workspace/YUP_WEB/file/SQLLITE_YAMA.db"               , "yama"     , "new1234" );
     *  Connection conn = getConnection("org.hsqldb.jdbcDriver"                        , "jdbc:hsqldb:mem:yama"                                                , "yama"     , "new1234" );
     *
     *  String query = "select * from simple_board";
     *  List<Map<String, Object>> results = excute(conn, query, null, 0);
     *  CommonUtil.print(results);
     *  conn.close();
     *
     * </PRE>
     *
     * @location org.noru.work.alone.StdDataBaseExcute.java
     * @Method getConnection
     * @cdate 2015. 10. 2. ï¿½ã…½?‘ 4:30:34
     * @version 1.0
     * @author Administrator
     * @param driverName
     * @param url
     * @param user
     * @param password
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Connection getConnection(String driverName, String url, String user, String password) throws SQLException, ClassNotFoundException
    {
        Class.forName(driverName);
        return DriverManager.getConnection(url, user, password);
    }
}
