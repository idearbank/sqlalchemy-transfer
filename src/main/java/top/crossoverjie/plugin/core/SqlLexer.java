package top.crossoverjie.plugin.core;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2020-03-02 22:29
 * @since JDK 1.8
 */
public class SqlLexer {

    private List<TokenResult> results = new ArrayList<>();

    public List<TokenResult> tokenize(String script) throws IOException {

        CharArrayReader reader = new CharArrayReader(script.toCharArray());
        TokenType status = TokenType.INIT;
        int ch;
        char value;
        TokenResult result = new TokenResult();
        while ((ch = reader.read()) != -1) {
            value = (char) ch;
            switch (status) {
                case INIT:
                    result = initToken(value, result);
                    status = result.tokenType ;
                    break;
                case COMMAND:
                    if (isLetterNotDdlKey(value)){
                        result.text.append(value);
                    }else {
                        status = TokenType.INIT;
                    }
                    break;
                case FIELD:
                    if(value == '`'){
                        status = TokenType.INIT;

                        // 将结尾的字符串 ` 写入
                        result.text.append(value);
                    }else if (isLetter(value)){
                        result.text.append(value);
                    }
                    break;

                case FIELD_TYPE:
                    if (value == 't' || value =='l' || value == 'e' || value=='r'){
                        status = TokenType.INIT;

                        //结尾字母写入
                        result.text.append(value);
                    }else {
                        result.text.append(value);
                    }
                    break;
                case FIELD_LEN:
                    if (value == ')'){
                        status = TokenType.INIT;

                        //结尾字母写入
                        result.text.append(value);
                    }else {
                        result.text.append(value);

                        if (value == '\n'){
                            // 换行符的字符单独额外出来
                            result = new TokenResult();
                            status = TokenType.INIT ;
                        }
                    }
                    break;
                case COMMENT:
                    if (value == '\''){
                        status = TokenType.INIT;

                        //结尾字母写入
                        result.text.append(value);
                    }else {
                        result.text.append(value);
                    }

                default:
                    break;

            }
        }
        if (result.getText().length() > 0) {
            results.add(result);
        }


        return results;
    }

    /**
     *  ddl 关键字前缀 `` int decimal varchar
     */
    private static final char[] keep_char_prefix = new char[]{'`','i','d','v'} ;

    private TokenResult initToken(char value, TokenResult result) {

        //再次调用初始化的时候一定是状态转移后，说明可以写入一个完整的数据了。
        if (result.getText().length() > 0){
            results.add(result) ;
            result = new TokenResult() ;
        }

        if (isLetterNotDdlKey(value)){
            result.tokenType = TokenType.COMMAND;
            result.text.append(value);
        }else if (value == '`'){
            result.tokenType = TokenType.FIELD;
            result.text.append(value);
        }else if (value == 'i' || value == 'd' || value == 'v'){
            result.tokenType = TokenType.FIELD_TYPE;
            result.text.append(value);
        }else if (value == '('){
            result.tokenType = TokenType.FIELD_LEN;
            result.text.append(value);
        }else if (value == '\''){
            result.tokenType = TokenType.COMMENT;
            result.text.append(value);
        }
        else {
            result.tokenType = TokenType.INIT;
        }


        return result ;

    }


    /**
     * 是否字母，但不能是关键字
     * @param value
     * @return
     */
    private boolean isLetterNotDdlKey(int value) {
        for (char prefix : keep_char_prefix) {
            if (prefix == value){
                return false ;
            }
        }
        return isLetter(value) ;
    }

    /**
     * 是否字母
     * @param value
     * @return
     */
    private boolean isLetter(int value){
        return value >= 65 && value <= 122;
    }

    /**
     * whether digit
     * @param value
     * @return
     */
    private boolean isDigit(int value) {
        return value >=48 && value <= 57;
    }


    public class TokenResult {
        private Text text = new Text();
        private TokenType tokenType ;

        public Text getText() {
            return text;
        }

        public void setText(Text text) {
            this.text = text;
        }

        public TokenType getTokenType() {
            return tokenType;
        }

        public void setTokenType(TokenType tokenType) {
            this.tokenType = tokenType;
        }
    }

    public class Text {
        private StringBuilder text = new StringBuilder();

        public void append(char value){
            text.append(value) ;
        }

        public int length(){
            return this.text.length() ;
        }

        @Override
        public String toString() {
            return this.text.toString() ;
        }
    }
}