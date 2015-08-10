import java.util.HashSet;
import java.util.Set;

/**
 * Created by mikhail on 08.08.15.
 */
class EntityTransformerUtil
{
  private static Set<Class> primitives = new HashSet<>( );
  private static Set<Class> wrappers = new HashSet<>( );

  static {
    primitives.add( boolean.class );
    primitives.add( byte.class );
    primitives.add( char.class );
    primitives.add( double.class );
    primitives.add( float.class );
    primitives.add( int.class );
    primitives.add( long.class );
    primitives.add( short.class );
    primitives.add( void.class );

    wrappers.add( Boolean.class );
    wrappers.add( Byte.class );
    wrappers.add( Character.class );
    wrappers.add( Double.class );
    wrappers.add( Float.class );
    wrappers.add( Integer.class );
    wrappers.add( Long.class );
    wrappers.add( Short.class );
    wrappers.add( Void.class );
    wrappers.add( String.class );
  }

  public static boolean isPrimitive( Object entity ) {
    Class entityClass = entity.getClass();
    return isPrimitiveClass(entityClass);
  }

  public static boolean isPrimitiveClass(Class clazz) {
    return isPrimitiveType(clazz) || isPrimitiveWrapperType(clazz);
  }

  private static boolean isPrimitiveType(Class clazz) {
    return primitives.contains(clazz);
  }

  private static boolean isPrimitiveWrapperType(Class clazz) {
    return wrappers.contains(clazz);
  }
}
