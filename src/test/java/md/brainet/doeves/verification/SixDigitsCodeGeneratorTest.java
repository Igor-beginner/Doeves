package md.brainet.doeves.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SixDigitsCodeGeneratorTest {

    SixDigitsCodeGenerator sixDigitsCodeGenerator;

    @BeforeEach
    void setUp() {
        sixDigitsCodeGenerator = new SixDigitsCodeGenerator();
    }

    @Test
    void generateOne() {
        //given
        var expectedCodeLength = 6;

        //when
        String code = sixDigitsCodeGenerator.generate();

        //then
        assertEquals(expectedCodeLength, code.length());
    }

    @Test
    void generateList() {
        //given
        var expectedCodeLength = 6;

        //when
        List<String> codes = Stream.generate(sixDigitsCodeGenerator::generate)
                .limit(1_000_000)
                .toList();

        //then
        codes.forEach(s -> assertEquals(expectedCodeLength, s.length()));
    }
}