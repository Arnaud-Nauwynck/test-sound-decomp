package fr.an.sounddecomp.core.frag.def.functordef.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.collections.Factory;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;
import fr.an.sounddecomp.core.frag.def.functordef.TimeFragFunctor;
import fr.an.sounddecomp.core.frag.def.functordef.TimeFragFunctorDef;

/**
 * TimeFragFunctorDef sub-class for instanciating simple user-defined functor (wrapped in UserDefinedTimeFragFunctor)
 *
 * @param <T>
 */
public class UserDefinedTimeFragFunctorDef<T extends Runnable> extends TimeFragFunctorDef {

    public static class Builder<T> extends TimeFragFunctorDef.Builder {
        Class<T> objectType;
        Factory/*<T>*/ factory;
        
        @Override /** override to check sub-class parameter */
        public void addInputParamDef(TimeFragFunctorParamDef.Builder p) {
            if (!(p instanceof UserDefinedTimeFragFunctorParamDef.Builder)) throw new IllegalArgumentException();
            super.addInputParamDef(p);
        }
        @Override /** override to check sub-class parameter */
        public void addOutputParamDef(TimeFragFunctorParamDef.Builder p) {
            if (!(p instanceof UserDefinedTimeFragFunctorParamDef.Builder)) throw new IllegalArgumentException();
            super.addOutputParamDef(p);
        }
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * the underlying user-defined object class to wrap
     */
    private final Class<T> objectType;
    
    /**
     * factory for creating the underlying user-defined object instances to wrap
     */
    private final Factory/*<T>*/ factory;
    
    // ------------------------------------------------------------------------
    
    public UserDefinedTimeFragFunctorDef(TimeFragmentDefRegisty owner, String name, UserDefinedTimeFragFunctorDef.Builder<T> builder) {
        super(owner, name, builder);
        this.objectType = builder.objectType;
        this.factory = builder.factory;
        if (objectType == null) throw new IllegalArgumentException(); 
        if (factory == null) throw new IllegalArgumentException(); 
    }

    @Override
    public TimeFragFunctor createInstance() {
        @SuppressWarnings("unchecked")
        T wrappedObject = (T) factory.create();
        return new UserDefinedTimeFragFunctor<T>(this, wrappedObject);
    }
    
    
    
    // ------------------------------------------------------------------------
    
    /**
     * child ParamDef sub-class for UserDefinedTimeFragFunctor
     * it extends TimeFragFunctorParamDef only for performance reason: to cache introspection method/fields!! 
     */
    public static class UserDefinedTimeFragFunctorParamDef extends TimeFragFunctorParamDef {
        
        public static class Builder extends TimeFragFunctorParamDef.Builder {
            @Override
            public UserDefinedTimeFragFunctorParamDef build(TimeFragFunctorDef owner) {
                return new UserDefinedTimeFragFunctorParamDef((UserDefinedTimeFragFunctorDef<?>) owner, this);
            }
        }

        // private final PropertyDescriptor propDescriptor;
        private final Method propertyReadMethod; 
        private final Method propertyWriteMethod; 
        private final Field propertyField;
        
        public UserDefinedTimeFragFunctorParamDef(UserDefinedTimeFragFunctorDef<?> owner, UserDefinedTimeFragFunctorParamDef.Builder b) {
            super(owner, b);
            // resolve introspection java.lang.Field + getter/setter corresponding to parameterDef
            PropertyDescriptor foundProp = null;
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(owner.objectType);
                PropertyDescriptor[] propDescrs = beanInfo.getPropertyDescriptors();
                for(PropertyDescriptor propDescr : propDescrs) {
                    if (propDescr.getName().equalsIgnoreCase(name)) {
                        foundProp = propDescr;
                        break;
                    }
                }
            } catch (IntrospectionException ex) {
                throw new RuntimeException("Failed", ex);
            }

            Field foundPropertyField;
            try {
                foundPropertyField = owner.objectType.getField(name);
            } catch (NoSuchFieldException e) {
                foundPropertyField = null;
            } catch (SecurityException e) {
                foundPropertyField = null;
            }

            this.propertyReadMethod = (foundProp != null)? foundProp.getReadMethod() : null;
            this.propertyWriteMethod = (foundProp != null)? foundProp.getWriteMethod() : null;
            this.propertyField = foundPropertyField;
            if (isInput 
                    && (propertyWriteMethod == null || !Modifier.isPublic(propertyWriteMethod.getModifiers()))
                    && (propertyField == null || !Modifier.isPublic(propertyField.getModifiers()))
                    ) throw new IllegalStateException("missing public setter for inputParam " + name);
            if (!isInput
                    && (propertyReadMethod == null || !Modifier.isPublic(propertyReadMethod.getModifiers()))
                    && (propertyField == null || !Modifier.isPublic(propertyField.getModifiers()))
                    ) throw new IllegalStateException("missing public getter for ouputParam " + name);
        }

        public Method getPropertyReadMethod() {
            return propertyReadMethod;
        }

        public Method getPropertyWriteMethod() {
            return propertyWriteMethod;
        }

        public Field getPropertyField() {
            return propertyField;
        }
        
        public Object getFieldValue(Object target) {
            Object res;
            if (propertyReadMethod != null) {
                try {
                    res = propertyReadMethod.invoke(target, new Object[0]);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to get field '" + name + "' value", ex);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException("Failed to get field '" + name + "' value", ex);
                } catch (InvocationTargetException ex) {
                    Throwable nested = ex.getCause();
                    if (nested instanceof RuntimeException) {
                        throw (RuntimeException) nested;
                    } else {
                        throw new RuntimeException("Failed to get field '" + name + "' value", nested);
                    }
                }
            } else {
                try {
                    res = propertyField.get(target);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException("Failed to get field '" + name + "' value", ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to get field '" + name + "' value", ex);
                }
            }
            return res;
        }
        
        public void setFieldValue(Object target, Object value) {
            if (propertyWriteMethod != null) {
                try {
                    propertyWriteMethod.invoke(target, new Object[] { value });
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to set field '" + name + "' value", ex);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException("Failed to set field '" + name + "' value", ex);
                } catch (InvocationTargetException ex) {
                    Throwable nested = ex.getCause();
                    if (nested instanceof RuntimeException) {
                        throw (RuntimeException) nested;
                    } else {
                        throw new RuntimeException("Failed to set field '" + name + "' value", nested);
                    }
                }
            } else {
                try {
                    propertyField.set(target, value);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException("Failed to set field '" + name + "' value", ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to set field '" + name + "' value", ex);
                }
            }
        }
        
    }
    
}