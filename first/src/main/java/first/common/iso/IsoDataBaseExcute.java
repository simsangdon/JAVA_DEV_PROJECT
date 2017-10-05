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
     *  ?��?��?��?����?��뻾�?��?��. SqlVo �먯�? 紐⑤�? �먮?����媛�?����?��?��?��?����뺤젣 �쒕?��.
     *  �ㅽ뻾�?��?�� ?��?��?���먯꽌��二?��꽍���?��?�� �섍?? ; 媛숈�? �먮?�� ��媛�?�����덈?��
     *  ?��몄옄���깆?�� �쒖?���쒕?��.
     * </PRE>
     *
     * @location org.noru.db.core.DataBaseExcuter.java
     * @cdate 2013. 8. 26.
     * @version 1.0
     * @author 諛뺤�?��
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
        // �꾨Т���곌껐���?��?���뚮?�� null 諛섑?��
        if (conn == null)
        {
            throw new SQLException("Connection is null !!");
        }
        // �ㅽ뻾��vo媛��놁?���뚮�? null 諛섑?��
        if (query == null)
        {
            throw new SQLException("query is null !!");
        }

        long procStartTime = System.currentTimeMillis(); // 泥섎?���쒓�? ?��?��?��

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
     *  ?��꾩��덉?��濡쒖쭅��嫄몃┛ �쒓�? 痢≪?���섍�? �꾨?�� 泥섎?�� ?��붾뵫���댁＜硫��?��?��.
     *  EX)
     *  long procStartTime = System.currentTimeMillis();
     *  ...
     *  CommonUtil.procTime(procStartTime);
     * </PRE>
     *
     * @optitle
     * @cdate 2012. 3. 26.
     * @version 1.0
     * @author 諛뺤�?��
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
     * INSERT, UPDATE, DELETE ?��몄쓣 �ㅽ뻾�?��궓��?
     * </PRE>
     *
     * @location org.noru.db.core.DataBaseExcuter.java
     * @cdate 2013. 8. 26.
     * @version 1.0
     * @author 諛뺤�?��
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
     * 1. DDL(Data Definition Language) �곗?���곗�? 洹�?��?��?�瑜��뺤쓽�⑸?����
     *    1) CREATE   : �곗?���곕쿋�?��?�� 媛앹껜瑜��?���?�⑸?����
     *    2) DROP     : ��?���곕쿋�?��?�� 媛앹껜瑜���젣�⑸?����
     *    3) ALTER    : 湲곗?����?�댁?���섎?�� �곗?���곕쿋�?��?�� 媛앹껜瑜��?��?�� �뺤?���섎?�� ��븷���?��?����
     *    4) RENAME   : �곗?���곕쿋�?��?����?�щ읆紐낆?�� 蹂�꼍�?��?����
     *    5) TRUNCATE : �뚯?��?��붿쓣 理쒖?�� �앹�?��?��?��린�곹깭濡�留뚮뱾硫�? ROLLBACK��?��?����ν�?�덈?��.
     * 2. DCL(Data Control Language) �곗?���곕쿋�?��?�� �ъ슜�먯?�� 沅뚰븳���?��뼱�?��?����
     *    1) GRANT : �곗?���곕쿋�?��?�� 媛앹껜��沅?��븳��遺�뿬�⑸?����
     *    2) REVOKE : ��?�? ?���뿬���곗?���곕쿋�?��?�� 媛앹껜��沅?��븳��痍?��?���⑸?����
     * </PRE>
     *
     * @location org.noru.db.core.DataBaseExcuter.java
     * @cdate 2013. 8. 27.
     * @version 1.0
     * @author 諛뺤�?��
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
        pstmt.close(); // ?��?�냼�ㅻ�? �レ�? 以�?��

        return true;
    }

    private static List<Map<String, Object>> excuteProcedure(Connection conn, String query, Map<String, ? extends Object> params) throws SQLException
    {
        List<Map<String, Object>> result = null;

        ResultSet resultSet = null;

        PreparedStatement pstmt = getPreparedStatement(conn, query, params, PROCEDURE);

        Map<String, Object> outPut = new LinkedHashMap<String, Object>();

        /*--------------------------------------------------------------------------------------------------------------
         * �꾨줈�?����몃뜲 ResultSet ��諛섑?���섎?��寃쎌?�� rs媛�null���꾨?����
         *------------------------------------------------------------------------------------------------------------*/
        if (pstmt.execute())
        {
            resultSet = pstmt.getResultSet();
        }

        List<String> varList = new ArrayList<String>();
        setQuery(query, varList);

        /*--------------------------------------------------------------------------------------------------------------
         * �꾨줈�?������몄닔濡��섍꺼以�媛믩뱾�����? 泥섎?��?����?�����?
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
         * 留뚯?�� ResultSet ���덈?�� 寃쎌?������??���섎?�� 寃곌?��?����?��뼱�?��?��.
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
         * ?��?�냼�ㅻ�? �レ�? 以�?��
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
     * ResultSet ��List<Map<String, Object>> �뺥�?濡�留뚮뱺��? ��?�? �ㅻ�? ��Ц�먮�? 紐⑤�? 留뚮뱺��?
     * </PRE>
     *
     * @location org.noru.database.DataBaseUtil.java
     * @cdate 2014. 4. 16.
     * @version 1.0
     * @author 諛뺤�?��
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

        // ?�щ읆�⑥?�� 諛섎?�� �섎?����留듭?�� 留뚮뱾��以�떎.
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

        return resultList; // 寃곌?��?���諛?��?���쒕?��.
    }

    private static final Map<String, Object> rsToMap(ResultSet resultSet, String keys[]) throws SQLException
    {
        if (resultSet == null)
        {
            return null;
        }
        // 硫�?�瑜��?���? �⑤?��
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cCnt = metaData.getColumnCount();

        // ?�щ읆�⑥?�� 諛섎?�� �섎?����留듭?�� 留뚮뱾��以�떎.
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 1; i <= cCnt; i++)
        {
            // �ㅻ�? 留뚮뱾��以�떎
            String key;
            if (keys == null || cCnt - 1 != keys.length)
            {
                key = metaData.getColumnName(i).trim().toUpperCase();
            } else
            {
                key = keys[i];
            }

            // 留뚯?�� 以묎컙��鍮꾩�?�쇰?�� Default濡�A, B, C, D, E ��媛숈�? Excel 湲곗����?�щ읆紐낆?�� ��??��
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
     *  �レ?��?����묒�?�щ읆 ��?쫫�?���? 蹂�?���쒗궓��?
     *  1��A 濡�蹂�?�� 2?���B濡�蹂�?��
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author 諛뺤�?��
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
     *  ?��?��븳���곗닔��泥섎?�� �����덇�? 蹂�꼍��?
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @Method numberToExcelColumnChars
     * @cdate 2015. 9. 16. �ㅽ?�� 3:05:58
     * @version 1.0
     * @author Administrator
     * @param input
     * @return
     */
    private static final String numberToExcelColumnChars(BigInteger input)
    {
        String rtnStr = "";

        BigInteger time = BigInteger.ZERO; // 紐�
        int rest = 0; // �섎?��吏�

        while (input.compareTo(uperCharsLength) == 1) // input > UPER_CHARS.length
        { // 26蹂�??�� �щ㈃

            time = input.divide(uperCharsLength); // 紐�
            rest = input.remainder(uperCharsLength).intValue(); // �섎?��吏�

            // CommonUtil.print(time, rest);
            if (time.compareTo(uperCharsLength.add(BigInteger.ONE)) > -1)
            { // 紐⑹?�� 27蹂�??�� �щ㈃
                if (rest == 0)
                { // �섎?��吏��? �놁?��硫��섎?��吏�?�� Z濡�移섑?���댁���?
                    rtnStr = UPER_CHARS[UPER_CHARS.length - 1] + rtnStr; // �섎?��吏��ｌ뼱二?��??
                    input = time.subtract(BigInteger.ONE); // Z濡�移섑?����寃껋?�� 類�紐⑹?�� �섍�? 以�?��.
                } else
                {
                    rtnStr = UPER_CHARS[rest - 1] + rtnStr; // �섎?��吏��? �ｌ�? 二쇨?? �ㅼ?����?�꾩�?
                    input = time; // 紐⑹?�� �섍�? 以�?��.
                }
            } else
            { // 26 �댄�?
                if (rest == 0)
                { // �섎?��吏��? �놁?��硫��섎?��吏�?�� Z濡�移섑?���댁���?
                    rtnStr = UPER_CHARS[UPER_CHARS.length - 1] + rtnStr; // �섎?��吏��ｌ뼱二?��??
                    rtnStr = UPER_CHARS[time.subtract(BigInteger.valueOf(2)).intValue()] + rtnStr; // Z濡�移섑?����媛�?�� ?��?��＜�?��紐⑸룄洹몃깷 泥섎?�� �댁�?
                } else
                {
                    rtnStr = UPER_CHARS[rest - 1] + rtnStr; // �섎?��吏��ｌ뼱二?��??
                    rtnStr = UPER_CHARS[time.subtract(BigInteger.ONE).intValue()] + rtnStr; // 紐⑸룄洹몃깷 泥섎?�� �댁�?
                }

                input = BigInteger.ZERO;
            }
        }

        if (input.compareTo(BigInteger.ZERO) == 1)
        { // 0�쇰�? 紐⑤�? 泥섎?�� �쒓쾬�??�濡��?��＜吏��?��?����
            rtnStr += UPER_CHARS[input.subtract(BigInteger.ONE).intValue()]; // 26蹂�??��媛숆굅���묒�寃껋?�� 泥섎?�� �댁���?
        }

        return rtnStr;
    }

    /**
     * �낅?����媛�?�씠 null �먮?�� null String ��寃쎌?�� true?���return �쒕?��.
     *
     * <pre>
     *
     * [�ъ슜 �덉?��]
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
     *  rs���곗?���곕�? object濡쒕�寃?��?���⑤?��.
     * </PRE>
     *
     * @location org.noru.converter.ConverterDatabase.java
     * @cdate 2013. 9. 4.
     * @version 1.0
     * @author 諛뺤�?��
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
                return longVarcharToString(rs.getCharacterStream(colNum)); // �ㅽ?��?��?��?�� �쎌뼱��?..
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
                return blobToBytes(rs.getBlob(colNum)); // Blob��byte[]濡�諛섑?���쒕?��.
            }
            case Types.ARRAY: // 2003
            {
                return arrayToStringArray(rs.getArray(colNum)); // String[] �뺥�?濡�諛섑?���쒕?��.
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
                return rowIdToString(rs.getRowId(colNum)); // �곴����?��?��.
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
                // �쎌뼱���?��?��?��?��?�� ��?����踰꾪?��
                StringBuffer buff = new StringBuffer();
                char[] ch = new char[BUFFER_SIZE];
                int len = -1;
                while ((len = reader.read(ch)) != -1)
                {
                    buff.append(ch, 0, len);
                }
                // ��?����踰꾪?��?���String 濡�蹂�?��
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
        StringBuffer buff = new StringBuffer(); // �쎌뼱���?��?��?��?��?�� ��?����踰꾪?��
        char[] ch = new char[BUFFER_SIZE];
        int len = -1;
        while ((len = reader.read(ch)) != -1)
        {
            buff.append(ch, 0, len);
        }
        // ��?����踰꾪?��?���String 濡�蹂�?��
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
            // ��?���섎?���댄꽣瑜�泥?��?���섍�? �꾪�? �뗮?��

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
                // FetchSize 媛�8096, row�섍�? 400留뚭�? 媛쒖?����postgres�먯�? 34?����뺣룄��fatch�쒓컙��嫄몃┝
                pstmt.setFetchSize(BUFFER_SIZE);
                conn.setAutoCommit(false);
            }
        } else
        {
            // insert update ��洹몃�? ?�ㅻ컠�?��궓��?
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
     * �꾨?����媛숈?�� ��?쫫�?���? 李얜?����
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
     * @author 諛뺤�?��
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
     * �섏쨷��蹂�꼍���좎?���덉?��誘��? �꾨?����媛숈?�� map utile �먮꽔���붾떎.
     * </PRE>
     *
     * @location org.noru.database.DataBaseUtil.java
     * @cdate 2013. 9. 25.
     * @version 1.0
     * @author 諛뺤�?��
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
     * �ㅻ?��吏��? ?��?��?��?���PreparedStatement���곹�?�섍�? 蹂�꼍�?��?��.
     * </PRE>
     *
     * @location org.noru.database.SqlVo.java
     * @cdate 2013. 9. 23.
     * @version 1.0
     * @author 諛뺤�?��
     * @tag
     * @param query
     */
    private static String setQuery(CharSequence query, List<String> varList)
    {
        String originQuery = query.toString();

        varList = new ArrayList<String>();

        /*--------------------------------------------------------------------------------------------------------------
         * �ㅽ뻾�?��uery�먯꽌�?��쪟媛��좎닔 �덈?��寃껋�? �꾨?�� 泥섎?�� 蹂��?
         *------------------------------------------------------------------------------------------------------------*/
        String preparedStatementQuery = originQuery.replace("\t", "    ").replace("\r\n", "\n").replace("\r", "\n").trim();

        /*--------------------------------------------------------------------------------------------------------------
         * �ㅽ뻾�?��uery��紐⑤�? Comment?����?���? 洹명?�� ; ?��몄옣���쒖?�� 洹몃?��?��?���line��吏�?��以�?��.
         *------------------------------------------------------------------------------------------------------------*/
        preparedStatementQuery = removeVoidLine(removeSqlComment(preparedStatementQuery).replace(";", ""));

        /*--------------------------------------------------------------------------------------------------------------
         * PreparedStatement ?���泥?��?�� �섍�? �꾪�? #([a-zA-Z0-9_- ]+)# �⑦꽩���?��뼱瑜�??濡쒕�寃�?
         *------------------------------------------------------------------------------------------------------------*/
        Pattern ptmsPattern = Pattern.compile(REX_PARAM_PATTEN);
        Matcher ptmsMatcher = ptmsPattern.matcher(preparedStatementQuery);
        while (ptmsMatcher.find())
        {
            varList.add(ptmsMatcher.group(1).trim());
        }

        /*--------------------------------------------------------------------------------------------------------------
         * 蹂�?��?���紐?���? ?濡�蹂�꼍�?��?�� ?��?��?��?�����엯��寃곗?���쒕?��.
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
     *  �쒖�? 二쇱�? --, // 濡��쒖?���댁�? NEW_LINE 源뚯�留�洹몃━?��NEW_LINE�쇰�? ���?
     *  �щ윭以�二쇱꽍��寃?��?�� �꾨?��?����곕㈃ 800 �먭�? �섏뼱媛�臾몄옣�먯�? �먮?��媛��쒕?��.
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
            int endIndx = input.indexOf("*/", startIndx) + 2; // �꾨＼�몃뜳�ㅻ?�� �쒖?��蹂�??�� ?�ㅼ빞�?��?��

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
            // �꾨?�� ?��몄옣 �꾧?��吏��? 媛꾨?��.
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
          ?��몄옣���곗?��?��?�뒗 �쒓컙���?��?�� 嫄몃?��誘��? �섎?����?��꾧탳?����?��?��.
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
     *  媛�Database蹂�connection ?��몄옣
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
     * @cdate 2015. 10. 2. �ㅽ?�� 4:30:34
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
