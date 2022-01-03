package sjdbc.cats.effect

import sjdbc.core.syntax.ConnectionSyntax
import sjdbc.core.syntax.ResultSetSyntax
import sjdbc.core.syntax.SQLSyntax

package object syntax extends ConnectionSyntax with DataSourceSyntax with ResultSetSyntax with SQLSyntax
