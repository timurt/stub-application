package kz.beesoft.test;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import kz.beesoft.controller.MemberRegistration;
import kz.beesoft.model.Member;
import kz.beesoft.util.Resources;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MemberRegistrationTest {
   @Deployment
   public static Archive<?> createTestArchive() {
      return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClasses(Member.class, MemberRegistration.class, Resources.class)
            .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   @Inject
   MemberRegistration memberRegistration;


   @Test
   public void testRegister() throws Exception {
      Member newMember = memberRegistration.getNewMember();
      newMember.setName("Jane Doe");
      newMember.setEmail("jane@mailinator.com");
      newMember.setPhoneNumber("2125551234");
      memberRegistration.register();
      assertNotNull(newMember.getId());
   }

}
