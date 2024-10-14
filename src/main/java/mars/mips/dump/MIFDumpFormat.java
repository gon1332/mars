package mars.mips.dump;

import mars.Globals;
import mars.mips.hardware.*;

import java.io.*;
import java.nio.file.Files;
/*
Copyright (c) 2003-2008,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * The Memory Initialization File (.mif) VHDL-supported file format
 * This is documented for the Altera platform at
 * www.altera.com/support/software/nativelink/quartus2/glossary/def_mif.html.
 *
 * @author Pete Sanderson, Modified by Sandro Lortkipanidze
 * @version October 2024
 */
public class MIFDumpFormat extends AbstractDumpFormat {

    public enum Format {
        BIN, HEX, OCT, DEC, UNS
    }

    private Format addressFormat;
    private Format dataFormat;

    public MIFDumpFormat() {
        super("MIF", "MIF", "Written as Memory Initialization File (Altera)", "mif");
        configure(Format.HEX, Format.HEX); // Default configuration
    }

    /**
     * Configure the output format for address and data.
     *
     * @param addressFormat Format for address output
     * @param dataFormat    Format for data output
     */
    public void configure(Format addressFormat, Format dataFormat) {
        this.addressFormat = addressFormat;
        this.dataFormat = dataFormat;
    }

    /**
     * Write MIPS memory contents according to the Memory Initialization File
     * (MIF) specification.
     *
     * @param file         File in which to store MIPS memory contents.
     * @param firstAddress first (lowest) memory address to dump. In bytes but
     *                     must be on word boundary.
     * @param lastAddress  last (highest) memory address to dump. In bytes but
     *                     must be on word boundary. Will dump the word that starts at this address.
     * @throws AddressErrorException if firstAddress is invalid or not on a word boundary.
     * @throws IOException           if error occurs during file output.
     */
    public void dumpMemoryRange(File file, int firstAddress, int lastAddress) throws AddressErrorException, IOException {
        try (PrintStream out = new PrintStream(Files.newOutputStream(file.toPath()))) {
            out.println("DEPTH = " + ((lastAddress - firstAddress) / Memory.WORD_LENGTH_BYTES + 1) + ";");
            out.println("WIDTH = 32;");
            out.println("ADDRESS_RADIX = " + addressFormat.name() + ";");
            out.println("DATA_RADIX = " + dataFormat.name() + ";");
            out.println("CONTENT");
            out.println("BEGIN");

            for (int address = firstAddress; address <= lastAddress; address += Memory.WORD_LENGTH_BYTES) {
                Integer temp = Globals.memory.getRawWordOrNull(address);
                if (temp == null)
                    break;
                out.println("\t"+formatAddress((address - firstAddress) / Memory.WORD_LENGTH_BYTES) + " : " + formatData(temp) + ";");
            }

            out.println("END;");
        }
    }

    private String formatAddress(int address) {
        return formatValue(address, addressFormat);
    }

    private String formatData(int data) {
        return formatValue(data, dataFormat);
    }

    private String formatValue(int value, Format format) throws IllegalArgumentException {
        switch (format) {
            case BIN:
                return String.format("%32s", Integer.toBinaryString(value)).replace(' ', '0');
            case HEX:
                return String.format("%8s", Integer.toHexString(value)).replace(' ', '0');
            case OCT:
                return String.format("%11s", Integer.toOctalString(value)).replace(' ', '0');
            case DEC:
                return String.valueOf(value);
            case UNS:
                return String.valueOf(Integer.toUnsignedLong(value));
            default:
                throw new IllegalArgumentException("Invalid format: " + format);
        }
    }
}
