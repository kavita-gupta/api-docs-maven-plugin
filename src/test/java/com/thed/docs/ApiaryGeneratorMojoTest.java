package com.thed.docs;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.thed.apiaryGenerator.ApiaryGeneratorMojo;

/**
 * Created by smangal on 5/22/14.
 */
public class ApiaryGeneratorMojoTest {

    @Before
    public void setup(){
        File file = new File("target/apiary.apib");
        if(file.exists()){
            file.delete();
        }
    }

    @Test
    public void testExecute() throws Exception {
        File file = new File("target/apiary.apib");
        Assert.assertFalse(file.exists());
        ApiaryGeneratorMojo mojo = new ApiaryGeneratorMojo();
        mojo.execute();
        Assert.assertTrue(file.exists());
    }
}
