package md.brainet.doeves.verification;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SixDigitsCodeGenerator implements CodeGenerator {

    private static final int CODE_LENGTH = 6;
    private static final String MISSING = "0";

    @Override
    public String generate() {
        int code = (int)(Math.random() * Math.pow(10, CODE_LENGTH));
        int dif = CODE_LENGTH - countDigitsIn(code);
        String emptyDigitsBefore = MISSING.repeat(dif);
        return code == 0
                ? emptyDigitsBefore
                : emptyDigitsBefore.concat(String.valueOf(code));
    }

    private int countDigitsIn(int code) {
        return code == 0 ? 0 : 1 + countDigitsIn(code / 10);
    }
}
