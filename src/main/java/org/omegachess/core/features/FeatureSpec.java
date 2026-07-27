package org.omegachess.core.features;

public final class FeatureSpec
  {
    public static final String VERSION                = "pagan-features-v1";

    public static final int WIDTH                     = 12;
    public static final int HEIGHT                    = 12;
    public static final int SQUARES                   = WIDTH * HEIGHT;
    public static final int PLANES                    = 64;
    public static final int FEATURE_COUNT             = PLANES * SQUARES;

    public static final int VALID_MASK                = 0;

    public static final int OWN_OCCUPANCY_BASE        = 1;
    public static final int ENEMY_OCCUPANCY_BASE      = 9;

    public static final int OWN_ATTACK_BASE           = 17;
    public static final int ENEMY_ATTACK_BASE         = 25;

    public static final int OWN_ATTACK_COUNT          = 33;
    public static final int ENEMY_ATTACK_COUNT        = 34;

    public static final int OWN_XRAY_BASE             = 35;
    public static final int ENEMY_XRAY_BASE           = 38;

    public static final int OWN_PINNED                = 41;
    public static final int ENEMY_PINNED              = 42;
    public static final int OWN_PIN_RAYS              = 43;
    public static final int ENEMY_PIN_RAYS            = 44;

    public static final int CHECKERS                  = 45;
    public static final int KING_ESCAPES              = 46;

    public static final int OWN_KING_ZONE             = 47;
    public static final int ENEMY_KING_ZONE           = 48;
    public static final int OWN_KING_PRESSURE         = 49;
    public static final int ENEMY_KING_PRESSURE       = 50;

    public static final int OWN_MOBILITY              = 51;
    public static final int ENEMY_MOBILITY            = 52;

    public static final int OWN_DEFENDED              = 53;
    public static final int ENEMY_DEFENDED            = 54;
    public static final int OWN_UNDEFENDED_ATTACKED   = 55;
    public static final int ENEMY_UNDEFENDED_ATTACKED = 56;

    public static final int OWN_CASTLE_KINGSIDE       = 57;
    public static final int OWN_CASTLE_QUEENSIDE      = 58;
    public static final int ENEMY_CASTLE_KINGSIDE     = 59;
    public static final int ENEMY_CASTLE_QUEENSIDE    = 60;

    public static final int EN_PASSANT                = 61;
    public static final int MOVE_COUNTER              = 62;
    public static final int CHECKER_COUNT             = 63;

    public static final String[] PLANE_NAMES          = {"valid_mask",

                                                         "own_pawn_occupancy",
                                                         "own_knight_occupancy",
                                                         "own_champion_occupancy",
                                                         "own_wizard_occupancy",
                                                         "own_bishop_occupancy",
                                                         "own_rook_occupancy",
                                                         "own_queen_occupancy",
                                                         "own_king_occupancy",

                                                         "enemy_pawn_occupancy",
                                                         "enemy_knight_occupancy",
                                                         "enemy_champion_occupancy",
                                                         "enemy_wizard_occupancy",
                                                         "enemy_bishop_occupancy",
                                                         "enemy_rook_occupancy",
                                                         "enemy_queen_occupancy",
                                                         "enemy_king_occupancy",

                                                         "own_pawn_attacks",
                                                         "own_knight_attacks",
                                                         "own_champion_attacks",
                                                         "own_wizard_attacks",
                                                         "own_bishop_attacks",
                                                         "own_rook_attacks",
                                                         "own_queen_attacks",
                                                         "own_king_attacks",

                                                         "enemy_pawn_attacks",
                                                         "enemy_knight_attacks",
                                                         "enemy_champion_attacks",
                                                         "enemy_wizard_attacks",
                                                         "enemy_bishop_attacks",
                                                         "enemy_rook_attacks",
                                                         "enemy_queen_attacks",
                                                         "enemy_king_attacks",

                                                         "own_attack_count",
                                                         "enemy_attack_count",

                                                         "own_bishop_xray",
                                                         "own_rook_xray",
                                                         "own_queen_xray",
                                                         "enemy_bishop_xray",
                                                         "enemy_rook_xray",
                                                         "enemy_queen_xray",

                                                         "own_pinned",
                                                         "enemy_pinned",
                                                         "own_pin_rays",
                                                         "enemy_pin_rays",

                                                         "checkers",
                                                         "king_escapes",

                                                         "own_king_zone",
                                                         "enemy_king_zone",
                                                         "own_king_pressure",
                                                         "enemy_king_pressure",

                                                         "own_mobility",
                                                         "enemy_mobility",

                                                         "own_defended",
                                                         "enemy_defended",
                                                         "own_undefended_attacked",
                                                         "enemy_undefended_attacked",

                                                         "own_castle_kingside",
                                                         "own_castle_queenside",
                                                         "enemy_castle_kingside",
                                                         "enemy_castle_queenside",

                                                         "en_passant",
                                                         "move_counter",
                                                         "checker_count"};

    public static final String[] PIECE_NAMES          = {"pawn",
                                                         "knight",
                                                         "champion",
                                                         "wizard",
                                                         "bishop",
                                                         "rook",
                                                         "queen",
                                                         "king"};

    public static final int PIECE_TYPES               = 8;

    public static final int PIECE_PAWN                = 0;
    public static final int PIECE_KNIGHT              = 1;
    public static final int PIECE_CHAMPION            = 2;
    public static final int PIECE_WIZARD              = 3;
    public static final int PIECE_BISHOP              = 4;
    public static final int PIECE_ROOK                = 5;
    public static final int PIECE_QUEEN               = 6;
    public static final int PIECE_KING                = 7;

    public static final int XRAY_BISHOP = 0;
    public static final int XRAY_ROOK   = 1;
    public static final int XRAY_QUEEN  = 2;
    public static final int XRAY_TYPES  = 3;

    private FeatureSpec()
      {
      }

    public static int index(int plane, int tensorIndex)
      {
        return plane * SQUARES + tensorIndex;
      }

    public static int index(int plane, int row, int col)
      {
        return plane * SQUARES + row * WIDTH + col;
      }

    /* Check allocation and length; potentially throw an exception. */
    public static void validateBuffer(float[] buffer)
      {
        if(buffer == null)
          throw new IllegalArgumentException("Feature buffer cannot be null.");

        if(buffer.length < FEATURE_COUNT)
          throw new IllegalArgumentException("Feature buffer is too small: expected at least " + FEATURE_COUNT + " values, found " + buffer.length + ".");

        return;
      }
  }