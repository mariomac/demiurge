package es.bsc.demiurge.security;

import es.bsc.demiurge.security.tools.CryptTools;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
public class CipherTest {
	@Test
	public void sha256cipherTest() {
		String uncipheredPassword = "Som3c0o1P4ssw0rd";
		String cipheredPassword = "33fabeda97e41fc433f618aaf6882e59fabbb311659ad7ea3d4b12ffb4682675";
		assertTrue(CryptTools.verify(cipheredPassword.getBytes(), CryptTools.crypt(uncipheredPassword)));
		assertTrue(CryptTools.verify(uncipheredPassword.getBytes(), CryptTools.crypt(uncipheredPassword)));
	}



}
