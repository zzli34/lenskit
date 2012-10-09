/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.eval.config;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

public class TestEvalScriptConfig {
    EvalScriptConfig esc;
    Properties props;

    @Before
    public void initialize() {
        props = new Properties();
        props.setProperty("foo", "bar");
        props.setProperty("foo2", "bar");
        props.setProperty("foo3", "bar3");
        esc = new EvalScriptConfig(props);
    }

    /**
     * Make sure that changes to the Properties object don't change
     * the EvalScriptConfig after construction.
     */
    @Test
    public void testClone() {
        assertEquals("bar", esc.getProperty("foo"));
        props.setProperty("foo", "foobar");
        assertEquals("bar", esc.getProperty("foo"));
    }

    @Test
    public void testGet() {
        assertEquals("bar", esc.getProperty("foo"));
        assertEquals("bar", esc.getProperty("foo2"));
        assertEquals("bar3", esc.getProperty("foo3"));
        assertEquals(null, esc.getProperty("foo4"));
        assertEquals("bar5", esc.getProperty("foo5", "bar5"));
    }

    @Test
    public void testSpecialNamesDefaults() {
        assertEquals("eval.groovy", esc.getScript());
        assertEquals(".", esc.getAnalysisDir());
        assertEquals(".", esc.getDataDir());
    }

    @Test
    public void testSpecialNames() {
        props.setProperty("lenskit.eval.script", "../src/eval/eval.groovy");
        props.setProperty("lenskit.eval.dataDir", "../target/data/");
        props.setProperty("lenskit.eval.analysisDir", "../target/analysis/");

        esc = new EvalScriptConfig(props);

        assertEquals("../target/data/", esc.getDataDir());
        assertEquals("../target/analysis/", esc.getAnalysisDir());
        assertEquals("../src/eval/eval.groovy", esc.getScript());
    }

    @Test
    public void testDefaultNoForce() {
        assertThat(esc.force(), equalTo(false));
    }

    @Test
    public void testForce() {
        props.setProperty(EvalScriptConfig.FORCE_PROPERTY, "yes");
        esc = new EvalScriptConfig(props);
        assertThat(esc.force(), equalTo(true));
    }
}
