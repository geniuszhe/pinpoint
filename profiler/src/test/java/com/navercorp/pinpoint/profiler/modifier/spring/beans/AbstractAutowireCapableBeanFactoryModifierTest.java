/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.modifier.spring.beans;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupTransaction;
import com.navercorp.pinpoint.profiler.modifier.ModifierTransformAdaptor;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.modifier.Modifier;
import com.navercorp.pinpoint.test.ClassTransformHelper;
import com.navercorp.pinpoint.test.MockAgent;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

@Ignore
public class AbstractAutowireCapableBeanFactoryModifierTest {

    @Test
    public void test() throws Exception {
        DefaultAgent agent = MockAgent.of("pinpoint-spring-bean-test.config");

        RetransformEventTrigger trigger = mock(RetransformEventTrigger.class);
        MockByteCodeInstrumentor mockByteCodeInstrumentor = new MockByteCodeInstrumentor(agent.getByteCodeInstrumentor(), trigger);

        Modifier beanModifier = mock(Modifier.class);
        
        AbstractAutowireCapableBeanFactoryModifier modifier = AbstractAutowireCapableBeanFactoryModifier.of(mockByteCodeInstrumentor, agent.getProfilerConfig(), beanModifier);

        RetransformEventTrigger retransformEventTrigger = mockByteCodeInstrumentor.getRetransformEventTrigger();
        ClassLoader loader = getClass().getClassLoader();
        ClassTransformHelper.transformClass(loader, "org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory", modifier);
        
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans-test.xml");

        ModifierTransformAdaptor modifierTransformAdaptor = new ModifierTransformAdaptor(beanModifier);
        verify(retransformEventTrigger).retransform(Maru.class, modifierTransformAdaptor);
        verify(retransformEventTrigger).retransform(Morae.class, modifierTransformAdaptor);
        verify(retransformEventTrigger).retransform(Outer.class, modifierTransformAdaptor);
        verify(retransformEventTrigger).retransform(Inner.class, modifierTransformAdaptor);
        verify(retransformEventTrigger).retransform(ProxyTarget.class, modifierTransformAdaptor);
        verifyNoMoreInteractions(trigger);

        context.getBean("mozzi");
        context.getBean("mozzi");

        verify(retransformEventTrigger).retransform(Mozzi.class, modifierTransformAdaptor);
        verifyNoMoreInteractions(retransformEventTrigger);

        assertFalse(ProxyTarget.class.equals(context.getBean("proxyTarget").getClass()));
    }

    class MockByteCodeInstrumentor implements ByteCodeInstrumentor {
        private final ByteCodeInstrumentor delegate;
        private RetransformEventTrigger retransformEventTriggerMock;

        public MockByteCodeInstrumentor(ByteCodeInstrumentor delegate, RetransformEventTrigger retransformEventTriggerMock) {
            if (delegate == null) {
                throw new NullPointerException("delegate must not be null");
            }
            this.delegate = delegate;
            this.retransformEventTriggerMock = retransformEventTriggerMock;
        }

        @Override
        public InstrumentClass getClass(ClassLoader classLoader, String jvmClassName, byte[] classFileBuffer) throws InstrumentException {
            return delegate.getClass(classLoader, jvmClassName, classFileBuffer);
        }

        @Override
        public boolean findClass(ClassLoader classLoader, String javassistClassName) {
            return delegate.findClass(classLoader, javassistClassName);
        }

        @Override
        public InterceptorGroupTransaction getInterceptorGroupTransaction(String scopeName) {
            return delegate.getInterceptorGroupTransaction(scopeName);
        }

        @Override
        public InterceptorGroupTransaction getInterceptorGroupTransaction(InterceptorGroupDefinition scopeDefinition) {
            return delegate.getInterceptorGroupTransaction(scopeDefinition);
        }

        @Override
        public Class<?> defineClass(ClassLoader classLoader, String defineClass, ProtectionDomain protectedDomain) throws InstrumentException {
            return delegate.defineClass(classLoader, defineClass, protectedDomain);
        }

        @Override
        public Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN) throws InstrumentException {
            return delegate.newInterceptor(classLoader, protectedDomain, interceptorFQCN);
        }

        @Override
        public Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params, Class[] paramClazz) throws InstrumentException {
            return delegate.newInterceptor(classLoader, protectedDomain, interceptorFQCN, params, paramClazz);
        }

        @Override
        public void retransform(Class<?> target, ClassFileTransformer classEditor) {
            delegate.retransform(target, classEditor);
        }

        @Override
        public RetransformEventTrigger getRetransformEventTrigger() {
            return retransformEventTriggerMock;
        }
    }
}
