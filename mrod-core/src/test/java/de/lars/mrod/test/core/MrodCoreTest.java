package de.lars.mrod.test.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.lars.mrod.core.MrodCore;

public class MrodCoreTest {
	
	@Test
	public void initTest() {
		final MrodCore core = new MrodCore();
		core.initialize();
		// attempt to initialize a second time
		assertThrows(IllegalStateException.class, () -> core.initialize());
	}

}
