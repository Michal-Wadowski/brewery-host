package wadosm.breweryhost;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.apache.commons.codec.binary.Hex;

import static org.assertj.core.api.Assertions.assertThat;

class DriverInterfaceImplTest {

    @Nested
    @DisplayName("locking feature")
    class Lock {

        @Test
        void cannot_update_if_file_invalid_size() {
            // given
            DummyFile driverFile = new DummyFile(new byte[]{
                    0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            });

            DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

            // when
            boolean canUpdate = driverInterface.canUpdate();

            // then
            assertThat(canUpdate).isFalse();
        }

        @ParameterizedTest
        @CsvSource({"0x01,true", "0x02,true", "0x00,false", "0x03,false", "0x04,false"})
        void canUpdate(byte lockValue, boolean canLock) {
            // given
            DummyFile driverFile = blankValidFile();
            lockFile(driverFile, lockValue);

            DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

            // when
            boolean canUpdate = driverInterface.canUpdate();

            // then
            assertThat(canUpdate).isEqualTo(canLock);
        }

        @ParameterizedTest
        @CsvSource({"0x01,true", "0x02,true", "0x00,false", "0x03,false", "0x04,false"})
        void lock(byte lockValue, boolean canLock) {
            // given
            DummyFile driverFile = blankValidFile();
            lockFile(driverFile, lockValue);

            DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

            // when
            boolean lockResult = driverInterface.lock();

            // then
            if (canLock) {
                assertThat(lockResult).isTrue();
                assertThat(driverFile.getData()[0]).isEqualTo((byte) 0x2);
            } else {
                assertThat(lockResult).isFalse();
                assertThat(driverFile.getData()[0]).isNotEqualTo((byte) 0x2);
            }
        }

        @ParameterizedTest
        @CsvSource({"0x01,0x01", "0x02,0x01", "0x00,0x00", "0x03,0x03", "0x04,0x04"})
        void unlock(byte lockValue, byte afterUnlock) {
            // given
            DummyFile driverFile = blankValidFile();
            lockFile(driverFile, lockValue);

            DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

            // when
            driverInterface.unlock();

            // then
            assertThat(driverFile.getData()[0]).isEqualTo(afterUnlock);
        }

        @ParameterizedTest
        @CsvSource({"0x01,false", "0x02,true", "0x00,false", "0x03,false", "0x04,false"})
        void isLocked(byte lockValue, boolean isLocked) {
            // given
            DummyFile driverFile = blankValidFile();
            lockFile(driverFile, lockValue);

            DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

            // when
            boolean status = driverInterface.isLocked();

            // then
            assertThat(status).isEqualTo(isLocked);
        }
    }

    @ParameterizedTest
    @CsvSource({"0x00,true,0x01", "0x01,false,0x00", "0x01,true,0x01", "0x00,false,0x00"})
    void powerEnable(byte powerValue, boolean enable, byte afterPowerValue) {
        // given
        DummyFile driverFile = blankValidFile();
        lockFile(driverFile);
        driverFile.getData()[1] = powerValue;

        DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

        // when
        driverInterface.powerEnable(enable);

        // then
        assertThat(driverFile.getData()[1]).isEqualTo(afterPowerValue);
    }

    @ParameterizedTest
    @CsvSource({"0x02,0x01", "0x01,0x00"})
    void powerEnable_when_locked(byte lockValue, byte afterPowerValue) {
        // given
        DummyFile driverFile = blankValidFile();
        lockFile(driverFile, lockValue);

        DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

        // when
        driverInterface.powerEnable(true);

        // then
        assertThat(driverFile.getData()[1]).isEqualTo(afterPowerValue);
    }

    @ParameterizedTest
    @CsvSource({
            "000000, 1,true, 010000",
            "000000, 3,true, 000001",
            "000100, 2,false, 000000",
            "000000, 0,false, 000000",
            "000000, 4,true, 000000",
    })
    void motorEnable(String hexInitial, int motorNumber, boolean enable, String hexAfter) throws DecoderException {
        // given
        byte[] initialBytes = Hex.decodeHex(hexInitial);
        byte[] afterBytes = Hex.decodeHex(hexAfter);

        DummyFile driverFile = blankValidFile();
        lockFile(driverFile);

        System.arraycopy(initialBytes, 0, driverFile.getData(), 2, initialBytes.length);

        DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

        // when
        driverInterface.motorEnable(motorNumber, enable);

        // then
        DummyFile expectedDriverFile = blankValidFile();
        lockFile(expectedDriverFile);

        System.arraycopy(afterBytes, 0, expectedDriverFile.getData(), 2, afterBytes.length);

        assertThat(driverFile.getData()).isEqualTo(expectedDriverFile.getData());
    }

    @ParameterizedTest
    @CsvSource({"0x02,0x01", "0x01,0x00"})
    void motorEnable_when_locked(byte lockValue, byte afterPowerValue) {
        // given
        DummyFile driverFile = blankValidFile();
        lockFile(driverFile, lockValue);

        DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

        // when
        driverInterface.motorEnable(1, true);

        // then
        DummyFile expectedDriverFile = blankValidFile();

        assertThat(driverFile.getData()[2]).isEqualTo(afterPowerValue);
    }

    @ParameterizedTest
    @CsvSource({
            "0x0000, 0000",
            "0x1234, 1234",
            "0xffff, FFFF",
            "-1, 0000",
            "0x10001, 0000",
    })
    void playSound(int expectedValue, String hexValue) throws DecoderException {
        // given
        byte[] expectedBytes = Hex.decodeHex(hexValue);

        DummyFile driverFile = blankValidFile();
        lockFile(driverFile);

        DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

        // when
        driverInterface.playSound(expectedValue);

        // then
        DummyFile expectedDriverFile = blankValidFile();
        lockFile(expectedDriverFile);

        System.arraycopy(expectedBytes, 0, expectedDriverFile.getData(), 5, expectedBytes.length);
        assertThat(driverFile.getData()).isEqualTo(expectedDriverFile.getData());
    }

    @ParameterizedTest
    @CsvSource({"0x02,1234", "0x01,0000"})
    void playSound_when_locked(byte lockValue, String hexValue) throws DecoderException {
        // given
        DummyFile driverFile = blankValidFile();
        lockFile(driverFile, lockValue);

        DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

        // when
        driverInterface.playSound(0x1234);

        // then
        DummyFile expectedDriverFile = blankValidFile();
        lockFile(expectedDriverFile, lockValue);

        byte[] expectedBytes = Hex.decodeHex(hexValue);
        System.arraycopy(expectedBytes, 0, expectedDriverFile.getData(), 5, expectedBytes.length);
        assertThat(driverFile.getData()).isEqualTo(expectedDriverFile.getData());
    }

    @ParameterizedTest
    @CsvSource({
            "0000, 1, 0x7f, 7F00",
            "0000, 2, 0x7f, 007F",
            "1111, 1, 0x7f, 7F11",
            "1111, 2, 0x7f, 117F",

            "0000, -1, 0x7f, 0000",
            "0000, 3, 0x7f, 0000",
            "0000, 1, 0x101, 0000",
            "0000, 1, -1, 0000",
    })
    void setMainsPower(String hexInitial, int mainsNumber, int power, String hexValue) throws DecoderException {
        // given
        DummyFile driverFile = blankValidFile();
        lockFile(driverFile);
        byte[] initialBytes = Hex.decodeHex(hexInitial);
        System.arraycopy(initialBytes, 0, driverFile.getData(), 7, initialBytes.length);

        DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

        // when
        driverInterface.setMainsPower(mainsNumber, power);

        // then
        DummyFile expectedDriverFile = blankValidFile();
        lockFile(expectedDriverFile);

        byte[] expectedBytes = Hex.decodeHex(hexValue);
        System.arraycopy(expectedBytes, 0, expectedDriverFile.getData(), 7, expectedBytes.length);
        assertThat(driverFile.getData()).isEqualTo(expectedDriverFile.getData());
    }

    @ParameterizedTest
    @CsvSource({"0x02,7F00", "0x01,0000"})
    void setMainsPower_when_locked(byte lockValue, String hexValue) throws DecoderException {
        // given
        DummyFile driverFile = blankValidFile();
        lockFile(driverFile, lockValue);

        DriverInterface driverInterface = new DriverInterfaceImpl(driverFile);

        // when
        driverInterface.setMainsPower(1, 0x7f);

        // then
        DummyFile expectedDriverFile = blankValidFile();
        lockFile(expectedDriverFile, lockValue);

        byte[] expectedBytes = Hex.decodeHex(hexValue);
        System.arraycopy(expectedBytes, 0, expectedDriverFile.getData(), 7, expectedBytes.length);
        assertThat(driverFile.getData()).isEqualTo(expectedDriverFile.getData());
    }

    @Test
    void readBreweryStatus() {
    }

    private void lockFile(DummyFile driverFile, byte lockValue) {
        driverFile.getData()[0] = lockValue;
    }

    private void lockFile(DummyFile driverFile) {
        lockFile(driverFile, DriverInterfaceImpl.Lock.CONTROLLER_LOCK.getCode());
    }

    private DummyFile blankValidFile() {
        return new DummyFile(new byte[]{0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
    }

    private static class DummyFile implements DriverFile {

        private byte[] data;

        private long offset = 0;

        public DummyFile(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }

        @Override
        public void write(byte[] b) {
            for (byte chunk : b) {
                data[(int) offset++] = chunk;
            }
        }

        @Override
        public void seek(long pos) {
            offset = pos;
        }

        @Override
        public byte[] read(int size) {
            byte[] result = new byte[size];
            int resultOffset = 0;
            for (byte chunk : data) {
                offset++;
                result[resultOffset++] = chunk;
                if (resultOffset >= size) {
                    break;
                }
            }
            return result;
        }

        @Override
        public long length() {
            return data.length;
        }
    }
}