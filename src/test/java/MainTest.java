/*
 * MainTest.java Dec 06, 2014
 *
 * Copyright 2014 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import hu.fnf.devel.forex.Main;
import org.junit.Test;

/**
 * @author 212417040 (BUD,MiB)
 */
public class MainTest {

        @Test
        public void testExecute() throws Exception {
                Main main = new Main();
                Main.main( new String[] {MainTest.class.getResource( "config.properties" ).getPath()} );
        }
}
