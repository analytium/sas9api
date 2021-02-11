package com.codexsoft.sas.secure;

import com.codexsoft.sas.secure.models.LicenseCapabilities;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class LicenseChecker {

    private static final int[] Mod37BitPosition = {
            32, 0, 1, 26, 2, 23, 27, 0, 3, 16, 24, 30, 28, 11, 0, 13, 4,
            7, 17, 0, 25, 22, 31, 15, 29, 10, 12, 6, 0, 21, 14, 9, 5,
            20, 8, 19, 18
    };

    private int capabilities;
    private final String errors;

    public LicenseChecker(int capabilities, String errors) {
        this.capabilities = capabilities;
        this.errors = errors;
    }

    // the function just does this
    // return (capabilities & (1 << (targetLevel - 1)) != 0);
    // (check if capabilities has bit set a targetLevel position)
    // but in a little bit convoluted way... :)
    // see Bit Twiddling Hacks algorithm details
    // http://graphics.stanford.edu/~seander/bithacks.html#ZerosOnRightModLookup
    // IMPORTANT: the multiplier should not change lower 10 bits of a number,
    // i.e. be in form (k << 10) + 1 where k is any positive integer
    
    //OP
    //check(1147905, 1) - for /sas/ only
    //check(201729, 1) - level 1
    //check(17256449, 2) - level 2
    //check(5325825, 3) - level 3
    public boolean check(int multiplier, int targetLevel) {
        // because of the constant v never becomes negative - important for right bit shifts below
        int v = (multiplier * capabilities) & 0x7FFFFFFF;
        // race is ok here, since lower bits are not changed between modifications
        capabilities = v + (capabilities << 10);
        // determine amount of trailing zeros in v and shift it by the value
        while (v > 0) {
            int r = Mod37BitPosition[(-v & v) % 37];
            if (r > 0) {
                targetLevel -= r;
            } else {
                targetLevel -= 1;
                if (targetLevel == 0) {
                    return true;
                }
                r = 1;
            }
            v >>= r;
        }
        return false;
    }

    public List<LicenseCapabilities> getCapabilities() {
        int v = capabilities;
        // reverse bits in v
        int s = 32;
        int mask = ~0;
        while ((s >>= 1) > 0) {
            mask ^= (mask << s);
            v = ((v >> s) & mask) | ((v << s) & ~mask);
        }
        int level = 0;
        List<LicenseCapabilities> licenseCapabilities = new ArrayList<>();
        while (v != 0) {
            if (v < 0) {
                LicenseCapabilities capability = LicenseCapabilities.byLevel(level + 1);
                if (capability != null) {
                    licenseCapabilities.add(capability);
                }
            }
            level += 1;
            v *= 2;
        }
        return licenseCapabilities;
    }

    public String getErrors() {
        return errors;
    }
}
