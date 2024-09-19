package module;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.providers.oauth2.auth0.Auth0AuthProvider;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import service.MyResolver;
import service.MyUserService;

/**
 * Initial DI module.
 */
public class Auth0Module extends Module {
    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
                bind(Resolver.class).to(MyResolver.class),
                bind(MyUserService.class).toSelf().eagerly(),
                bind(Auth0AuthProvider.class).toSelf().eagerly()
        );
    }
}
