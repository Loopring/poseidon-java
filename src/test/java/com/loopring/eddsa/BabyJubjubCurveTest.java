package com.loopring.eddsa;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.*;

public class BabyJubjubCurveTest {

    @Test
    public void CheckInCurve() {
        EddsaPoint[] onCurvePoints = generateOnCurvePoints(100);

        for (EddsaPoint point : onCurvePoints) {
            assertTrue(BabyJubjubCurve.inCurve(point));
        }
    }

    @Test
    public void CheckPointAdd() {
        int size = 100;
        EddsaPoint[] onCurvePoints = generateOnCurvePoints(size);

        for (int i = 0; i < size; i+=2) {
            assertTrue(BabyJubjubCurve.inCurve(onCurvePoints[i]));
            assertTrue(BabyJubjubCurve.inCurve(onCurvePoints[i+1]));
            EddsaPoint newP = BabyJubjubCurve.addPoint(onCurvePoints[i], onCurvePoints[i+1]);
            assertTrue(BabyJubjubCurve.inCurve(newP));
        }
    }

    @Test
    public void CheckPointMuliply() {
        int size = 100;
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

//        for (int i = 0; i < size; i++) {
//            EddsaPoint newP = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, BigInteger.valueOf(i));
//            assertTrue(BabyJubjubCurve.inCurve(newP));
//        }

        Random r = new Random();
        byte[] randomBytes = new byte[BabyJubjubCurve.FIELD_SIZE];
        for (int i = 0; i < size; i++) {
            r.nextBytes(randomBytes);
            BigInteger s = new BigInteger(1, randomBytes);
            EddsaPoint newP = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, s);
            assertTrue(BabyJubjubCurve.inCurve(newP));
        }
    }

    @Test
    public void CheckPointMuliply_WNAF() {
        int size = 100;
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

        Random r = new Random();
        byte[] randomBytes = new byte[BabyJubjubCurve.FIELD_SIZE];
        for (int i = 0; i < size; i++) {
            r.nextBytes(randomBytes);
            BigInteger s = new BigInteger(1, randomBytes);
            EddsaPoint newP = BabyJubjubCurve.mulPointEscalar_wnaf(BabyJubjubCurve.base8, s);
//            assertTrue(BabyJubjubCurve.inCurve(newP));
        }
    }

    @Test
    public void ValidatePointMuliply_WNAF() {
        int size = 100;
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

        Random r = new Random();
        byte[] randomBytes = new byte[BabyJubjubCurve.FIELD_SIZE];
        for (int i = 0; i < size; i++) {
            r.nextBytes(randomBytes);
            BigInteger s = new BigInteger(1, randomBytes);
            EddsaPoint newP_wnaf = BabyJubjubCurve.mulPointEscalar_wnaf(BabyJubjubCurve.base8, s);
            EddsaPoint newP = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, s);
            assertEquals(newP, newP_wnaf);
        }
    }

    @Test
    public void validatePointCompress() {
        for (PointCompressionPair c : getPointCompressTestCases()) {
            FieldElement selfCompress = new FieldElement(BabyJubjubCurve.p).fromLeBuf(c.point.compress());
            assert BabyJubjubCurve.inCurve(c.point);
            assert selfCompress.equals(c.compressPt);
        }

    }

    private EddsaPoint[] generateOnCurvePoints(int size) {
        // a * x^2 + y^2 = 1 + d * x^2 * y^2
        assertTrue(BabyJubjubCurve.inCurve(BabyJubjubCurve.base8));

        EddsaPoint[] onCurvePts = new EddsaPoint[size];
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            onCurvePts[i] = BabyJubjubCurve.mulPointEscalar(BabyJubjubCurve.base8, new BigInteger(BabyJubjubCurve.BIT_FIELD_SIZE, r));
        }
        return onCurvePts;
    }


    class PointCompressionPair {
        public EddsaPoint point;
        public FieldElement compressPt;

        public PointCompressionPair(EddsaPoint pt, byte[] compressPt) {
            new PointCompressionPair(pt, new FieldElement(BabyJubjubCurve.p).fromLeBuf(compressPt));
        }

        public PointCompressionPair(EddsaPoint pt, FieldElement compressPt) {
            this.point = pt;
            this.compressPt = compressPt;
        }
    }

    private PointCompressionPair[] getPointCompressTestCases() {
        return new PointCompressionPair[]{
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("4009043684211763541535573272414681571521361376366296082221213549920039949112", 10),
                                new BigInteger("9742925533018339817888141345204841920150899349628605534158459953470785794587", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("9742925533018339817888141345204841920150899349628605534158459953470785794587", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("14631379960006914369238839597861778511164526324854514835271097969173508180562", 10),
                                new BigInteger("18360533817047488955917522174657466958617650357333268673735898976173580697030", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("18360533817047488955917522174657466958617650357333268673735898976173580697030", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("9937792724951018941673000792112765697191907030479610096939756228399062074844", 10),
                                new BigInteger("20976342795210138843496805608846472264102787021025593770095690120533986325609", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("20976342795210138843496805608846472264102787021025593770095690120533986325609", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("14364679431518672584904273327179480837813855114321042729240714840807399090438", 10),
                                new BigInteger("13072239485823698390203237577052198644544329718429479577109964557978562820123", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("13072239485823698390203237577052198644544329718429479577109964557978562820123", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("19379045467441499724234199302142801410976498871569352607461458046761897654831", 10),
                                new BigInteger("9415247489199257448833917426765938168583190977637128350708615943908386762605", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("67311292107857355160619409931109892095218183310457410370437407947864951582573", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("23540166793200489384462358458114476741871362286605940244074243986672840521", 10),
                                new BigInteger("16310866645302894228660285470858711677048965743782409809539914894489008132135", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("74206911263960991940445777975202665603683958076602691829268706898445572952103", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("4987707168389688319584174599528104344113622178952269070160528979733489583607", 10),
                                new BigInteger("5624155826454551230430925926389426871096646962800919536119536447468118207927", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("63520200445112648942216418430733380797731639295621201555848328451424683027895", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("819605605847416997051003972138236884339151306528596923847959493038435576306", 10),
                                new BigInteger("17112341825653867039070333294872992357224023364098744427979414311645908858649", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("17112341825653867039070333294872992357224023364098744427979414311645908858649", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("734708223870694331116547532118944528036885829250515392897024858219735073584", 10),
                                new BigInteger("5809276733575326280814379160628036297126814937001867241984101098525721844115", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("5809276733575326280814379160628036297126814937001867241984101098525721844115", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("14321399266248756259218327722578636616402907876291742153501850436634639233357", 10),
                                new BigInteger("8626252938427657680770343173497630140863143536108763867242659532714580925691", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("66522297557085755392555835677841584067498135868929045886971451536671145745659", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("10722889267039780027376576826612267872348928831825688002682065772851832139210", 10),
                                new BigInteger("10478383233745338241766365854391913112606521049912370873371098444897179977649", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("10478383233745338241766365854391913112606521049912370873371098444897179977649", 10)
                        )),
                new PointCompressionPair(
                        new EddsaPoint(
                                new BigInteger("17335106220938597338091203925933070493655087421062140629066199468904609579773", 10),
                                new BigInteger("13001456266324689073512999437234464385452690349656234380476520789844500972799", 10)),
                        new FieldElement(
                                BabyJubjubCurve.p,
                                new BigInteger("70897500884982786785298491941578418312087682682476516400205312793801065792767", 10)
                        ))
        };
    }

}
